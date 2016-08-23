/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pricing;

import java.util.*;
import gridsim.*;
import gridsim.util.*;


/**
 *
 * @author MORBiD
 */
public class generateResource {
    public static void main(String args[])
    {
        
        int totalResource = 5;
        int i = 0;
        GridResource res = null;
        

        
        
        // create a typical grid resource
        for (i = 0; i < totalResource; i++)
        {
            GridResource res = createGridResource("Res_" + i , resourceVector[i],
                             totalMachine, rating, baud_rate, propDelay, mtu);

            resInfoID[i] = res.get_id(); 
            // allocate this resource to a random regional GIS entity
            //gisIndex = random.nextInt(num_GIS);
            gisIndex = gisIndexArray[i];
            gis = (RegionalGIS) gisList.get(gisIndex);
            res.setRegionalGIS(gis);    // set the regional GIS entity
            // System.out.println(res.get_name() + " will register to " + gis.get_name());
            gisInfo = (GISInformation) gisInformationList.get(gisIndex);
            gisInfo.setGISResourceMember(i);
            gisInformationList.set(gisIndex, gisInfo);
            // put this resource into a list
            resList.add(res);
        }
        System.out.println();

    private static GridResource createGridResource(String name,
                int totalPE, int totalMachine, int rating,
                double baud_rate, double delay, int MTU)
    {
        // Here are the steps needed to create a Grid resource:
        // 1. We need to create an object of MachineList to store one or more
        //    Machines
        MachineList mList = new MachineList();

        for (int i = 0; i < totalMachine; i++)
        {
            // 2. Create one Machine with its id, number of PEs and rating
            mList.add( new Machine(i, totalPE, rating) );
        }

        // 3. Create a ResourceCharacteristics object that stores the
        //    properties of a Grid resource: architecture, OS, list of
        //    Machines, allocation policy: time- or space-shared, time zone
        //    and its price (G$/PE time unit).
        String arch = "Intel";      // system architecture
        String os = "Linux";        // operating system
        double time_zone = 10.0;    // time zone this resource located
        double cost = 3.0;          // the cost of using this resource

        ResourceCharacteristics resConfig = new ResourceCharacteristics(
                arch, os, mList, ResourceCharacteristics.SPACE_SHARED,
                time_zone, cost);

        // 4. Finally, we need to create a GridResource object.
        long seed = 11L*13*17*19*23+1;
        double peakLoad = 0.0;        // the resource load during peak hour
        double offPeakLoad = 0.0;     // the resource load during off-peak hr
        double holidayLoad = 0.0;     // the resource load during holiday

        // incorporates weekends so the grid resource is on 7 days a week
        LinkedList Weekends = new LinkedList();
        Weekends.add(new Integer(Calendar.SATURDAY));
        Weekends.add(new Integer(Calendar.SUNDAY));

        // incorporates holidays. However, no holidays are set in this example
        LinkedList Holidays = new LinkedList();
        GridResource gridRes = null;
        try
        {
            // creates a GridResource with a link
            Link link = new SimpleLink(name + "_link", baud_rate, delay, MTU);
            gridRes = new GridResource(name, link, seed, resConfig, peakLoad,
                                offPeakLoad, holidayLoad, Weekends, Holidays);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

       // System.out.println("Creating a Grid resource (name: " + name +
       //         " - id: " + gridRes.get_id() + " and PE: " + totalPE + ")");

        return gridRes;
    }

        
    }

}
