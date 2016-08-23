/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package GnerateTopologyDistribution;
// with fixed value for degree of conectivity


import RD_DLA.*;
import gridsim.*;
import gridsim.index.*;
import gridsim.net.*;
import java.util.*;
import gridsim.*;
import gridsim.index.*;
import gridsim.net.*;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author MORBiD
 */
public class generateTopologyFix
{
    public int connectionDegree;
    public int num_GIS;
    
        
    public static void main(String args[])
    {
        generateTopologyFix topol_obj = null;
        int num;
        int deg;
        int totalResource = 500;
        int scale = 1;
        String topologyScale;
        String mapRes;
        switch (scale)
            {
                case 0:
                {
                    topologyScale = "T5001.txt";
                    mapRes = "R500.txt";
                    num = 500;
                    deg = num - 1;
                    break;
                }

                case 1:
                {
                    topologyScale = "T1000.txt";
                    mapRes = "R1000.txt";
                    num = 1000;
                    deg = 999;
                    break;
                }
                
                case 2:
                {
                    topologyScale = "T1500.txt";
                    mapRes = "R1500.txt";
                    num = 1500;
                    deg = num - 1;
                    break;
                }
                
                case 3:
                {
                    topologyScale = "T2000.txt";
                    mapRes = "R2000.txt";
                    num = 2000;
                    deg = num - 1;
                    break;
                }
                case 4:
                {
                    topologyScale = "T2500.txt";
                    mapRes = "R2500.txt";
                    num = 2500;
                    deg = num - 1;
                    break;
                }
                case 5:
                {
                    topologyScale = "T3000.txt";
                    mapRes = "R3000.txt";
                    num = 3000;
                    deg = num - 1;
                    break;
                }
                case 6:
                {
                    topologyScale = "T4000.txt";
                    mapRes = "R4000.txt";
                    num = 4000;
                    deg = num - 1;
                    break;
                }
                case 7:
                {
                    topologyScale = "T5000.txt";
                    mapRes = "R5000.txt";
                    num = 5000;
                    deg = num - 1;
                    break;
                }
                default:
                {
                    topologyScale = "InvalidTopology";
                    mapRes = "InvalidMap";
                    num = 0;
                    deg = num - 1;
                    break;
                }
            }

        topol_obj = new generateTopologyFix(num,deg);         
        int [] gisIndexArray = topol_obj.mapResource(totalResource);
        int [][] adjacencyMatrix = topol_obj.generateNetworkTopology(num,deg,topologyScale, gisIndexArray);
        //topol_obj.readNetworkTopology(num, deg, topologyScale);
    }

    
    
    
    public generateTopologyFix (int num_GIS, int connectionDegree)
    {
        this.num_GIS = num_GIS;
        this.connectionDegree = connectionDegree;
    }
    

    
    public int [][] generateNetworkTopology (int num_GIS, int connectionDegree,
            String topologyScale, int [] gisIndexArray)
    {
                // Construct Network Topology
        int i ,j ,k;
        int maxD = 100;
        int [][] adjacencyMatrix = new int [num_GIS][num_GIS];
        for (i = 0; i < num_GIS; i++)
            for (j = 0; j < num_GIS; j++)
                adjacencyMatrix[i][j] = 0;
        
        Random random = new Random();   // a random generator


        ArrayList possibleSrc = new ArrayList();
        int adjacencyNumel;
        int [] permutationSrc = new int [num_GIS];
        int permutationLength;
        int permutationIndex;
        int permutaionValue;
        adjacencyNumel = this.connectionDegree;
        for (i = 0; i < num_GIS; i++)
        {
            for (j = 0; j < num_GIS; j++)
            {
                if (j != i)
                {

                    possibleSrc.add(j);
                    //System.out.print(j + " ");
                }
            }
            
            //System.out.println();
            for (j = 0; j < num_GIS-1; j++)
            {
                permutationLength = possibleSrc.size();
                
                permutationIndex = random.nextInt(permutationLength);
                permutaionValue = ((Integer) possibleSrc.get(permutationIndex)).intValue();
                permutationSrc [j] = permutaionValue;
                possibleSrc.remove(permutationIndex);
                //System.out.print(permutationSrc [j] + " ");
            }
           //System.out.println();
            //System.out.println();
            for (j = 0; j < adjacencyNumel; j++)
            {
                adjacencyMatrix [i][permutationSrc[j]] = 1;
            }
            
        }
/*       
        for (i = 0; i < num_GIS; i++)
        {
            for (j = 0; j < num_GIS; j++)
                System.out.print(adjacencyMatrix[i][j] + " ");
            System.out.println();
        }
*/ 
        // write network topology
       try {
            FileOutputStream fos = new FileOutputStream(topologyScale);
            PrintWriter pw = new PrintWriter(fos);
            for (i = 0; i < num_GIS; i++)
            {
                for (j = 0; j < num_GIS; j++)
                {
                    pw.print(adjacencyMatrix[i][j]);
                    if (j != num_GIS - 1)
                    {
                        pw.print(",");
                    }
                }
                pw.println();
                }
            pw.close();
            fos.close();
/*            
            // write resource map
            FileOutputStream fos1 = new FileOutputStream(mapRes);
            PrintWriter pw1 = new PrintWriter(fos1);
            for (i = 0; i < gisIndexArray.length; i++)
            {
                pw1.print(gisIndexArray[i]);
                if (i != gisIndexArray.length - 1)
                    pw1.print(",");
            }
            pw1.close();
            fos1.close();
*/                
//            }
            
            
            }
       

       
       
        catch (Exception ex)
            {
                Logger.getLogger(body3.class.getName()).log(Level.SEVERE, null, ex);
            }
        return adjacencyMatrix;
           
    }
    
     public int [][] readNetworkTopology (int num_GIS, int connectionDegree, String topologyScale)
    {
        int adjacencyMatrix [][]=new int[num_GIS][num_GIS];
        int i,j;
        try
        {
            FileReader fr=new FileReader(topologyScale);
            BufferedReader br=new BufferedReader(fr);
            String line;
            for (i = 0; i < num_GIS; i++)
            {
                line = br.readLine();
                String[] values=line.split(",");
                for (j=0;j<num_GIS;j++)
                {
                    adjacencyMatrix[i][j]=Integer.parseInt(values[j]);
                }
            }
            br.close();
            fr.close();
        }
        catch (Exception ex)
            {
                Logger.getLogger(body3.class.getName()).log(Level.SEVERE, null, ex);
            }
        
/*       
        for (i = 0; i < num_GIS; i++)
        {
            for (j = 0; j < num_GIS; j++)
                System.out.print(adjacencyMatrix[i][j] + " ");
            System.out.println();
        }
*/      
        return adjacencyMatrix;

    }
     
     public int [] mapResource (int totalResource)
     {
        Random random = new Random();   // a random generator            
        int i = 0;
        int [] gisIndexArray = new int [totalResource];
        for (i = 0; i < totalResource; i++)
             gisIndexArray[i] = random.nextInt(this.num_GIS);
        return gisIndexArray;
     }
    
    

}
