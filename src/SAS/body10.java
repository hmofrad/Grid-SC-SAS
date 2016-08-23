package pricing;


/**
 *
 * @author MORBiD
 * a test example for dynamic pricing 
 * using super-scheduling algorithm 
 * for computational grids
 * َAdaptive Cooperative Super Scheduling
 */

import gridsim.*;
import gridsim.net.*;
import gridsim.util.Workload;
import java.util.*;
import gridsim.index.*;
import gridsim.GridUser.*;
import gridsim.index.AbstractGIS;
import gridsim.net.Link;


import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintWriter;


/**
 * Test Driver class for this example
 */
public class body10
{
    /**
     * Creates main() to run this example
     */
    public static void main(String[] args)
    {        
        try
        {
            //////////////////////////////////////////
            // Step 1: Initialize the GridSim package. It should be called
            // before creating any entities. We can't run this example without
            // initializing GridSim first. We will get run-time exception
            // error.
            
            // number of grid user entities + any Workload entities.
            int num_user = 1;
            int totalResource;
            int num_GFA;
            //int num_GFA = 6;              // number of GFA in the network
            //int totalResource = 10;         // total number of grid resources
            //int num_GFA = totalResource;              // number of GFA in the network
            Calendar calendar = Calendar.getInstance();

            // a flag that denotes whether to trace GridSim events or not.
            boolean trace_flag = false;

            // Initialize the GridSim package
            System.out.println("Initializing GridSim package");
            GridSim.init(num_user, calendar, trace_flag);

            //////////////////////////////////////////
            // Step 2: Creates one or more GridResource entities

            // baud rate and MTU must be big otherwise the simulation
            // runs for a very long period.
            double baud_rate = 1000000;     // 1 Gbits/sec
            double propDelay = 10;   // propagation delay in millisecond
            int mtu = 100000;        // max. transmission unit in byte
            int i = 0;
            int j = 0;
            int k = 0;
            
            int scale = 0;
            // 0   1    2     3   4    5    6    7
            // 100 500 1000 <== num_GFA
            // 100 500 1000 <== totalResource
            String topologyScale;
            String mapResource;
            switch (scale)
            {
                case 0: // Grid with 100 nodes
                {
                    topologyScale = "T100.txt";
                    mapResource = "R100.txt";
                    num_GFA = 100;
                    totalResource = 100;
                    break;
                }
                case 1: // Grid with 500 nodes
                {
                    topologyScale = "T500.txt";
                    mapResource = "R500.txt";
                    num_GFA = 100;
                    totalResource = 100;
                    break;
                }
                                
                case 2: // Grid with 1000 nodes
                {
                    topologyScale = "T1000.txt";
                    mapResource = "R1000.txt";
                    num_GFA = 1000;
                    totalResource = 1000;
                    break;
                }               
                default:
                {
                    topologyScale = "InvalidTopology";
                    mapResource = "InvalidMap";
                    num_GFA = 0;
                    totalResource = 0;
                    break;
                }
            }
            
            
            
            // Network Topology
            //String topologyScale = "T10.txt";
            int adjacencyMatrix [][]=new int[num_GFA][num_GFA];
            FileReader fr = new FileReader(topologyScale);            
            BufferedReader br = new BufferedReader(fr);
            String line;
            for (i = 0; i < num_GFA; i++)
            {
                line = br.readLine();
                String[] values=line.split(",");
                for (j=0; j<num_GFA; j++)
                    adjacencyMatrix[i][j]=Integer.parseInt(values[j]);
            }
            br.close();
            fr.close();
          
            //////////////////////////////////////////
            // Creates one or more regional GIS entities
            // the GFA enteties act as GFA entetis 
            // GFA enteties provide information about machines
            // which are accosiated to that clusetr of grid
            // GFA = Grid Federation Agent
            // GIS = Grid Information System
            
            RegionalGIS gfa = null;  // a regional GFA entity
            RegionalGFA gfaInfo;             // Grid Federation Agent (GFA) aditional entity
            

            ArrayList gfaList = new ArrayList();             // array
            ArrayList gfaInformationList = new ArrayList();  // array
            int [] gfaIDList = new int [num_GFA];
            
            for (i = 0; i < num_GFA; i++)
            {
                String gfaName = "Regional_GFA_" + i;   // regional GFA name

                // a network link attached to this regional GFA entity
                Link link = new SimpleLink(gfaName + "_link", baud_rate,propDelay, mtu);
                
                // create a new regional GFA entity
                gfa = new RegionalGIS(gfaName, link);
                gfaList.add(gfa);
                System.out.println(gfaName + " is created");
                gfaIDList[i] = gfa.get_id();
                
                // store extra information about GIS entity
                // add this GFA information into Lists
                // shared federation directory accumulate GFA information
                // in Regional_GFA class the resource information are gathered
                // then in shared federation directory this information
                // accumulate into a singular database for GFA queries
                // shared federation diresctory modeled as a linked list
                gfaInfo = new RegionalGFA(gfaName, gfa.get_id(), i);
                gfaInformationList.add(gfaInfo);
            }
            
            
            // Read Resource Distribution
            int    [] resourceProcessor = new int [totalResource];
            double [] resourcePrice = new double [totalResource];
            int    [] resourceRating = new int [totalResource];
            
            FileReader fr0=new FileReader(mapResource);
            BufferedReader br0=new BufferedReader(fr0);
            String line0;
            for (i=0; i < 3; i++)
            {
                line0 = br0.readLine();
                String[] values0=line0.split(",");
                if (i == 0)
                {
                    for (j = 0; j < totalResource; j++)
                        resourceProcessor[j]=Integer.parseInt(values0[j]);                
                }
                
                else if (i == 1)
                {
                    for (j = 0; j < totalResource; j++)
                        resourcePrice[j]=Integer.parseInt(values0[j]);                
                }

                else if (i == 2)
                {
                    for (j = 0; j < totalResource; j++)
                        resourceRating[j]=Integer.parseInt(values0[j]);                
                }
            }

            br0.close();
            fr0.close();
            

            // more resources can be created by
            // setting totalResource to an appropriate value
            // int totalResource = 6;  // total number of grid resources
            int totalMachine = 1;   // total number of machines in each resource
            // int [] totalPE = {64,31,7,18,77,333,64,31,7,18};        // total number of processing elements in each machine
            // int [] totalPE = {1,1,1,1,1,1,1,1,1,1};        // total number of processing elements in each machine
            // int [] rating = {850, 900, 700, 630, 930, 710,850, 900, 700, 630};         // an estimation CPU power
            // double [] price = {3128,1280,866,1582,1896,9000,3128,1280,866,1582};  // the cost of using this resource
            ArrayList resList = new ArrayList(totalResource);
            int [] resInfoID = new int [totalResource];           // resource ID vector
            Random random = new Random();   // a random generator
            int gfaIndex;
            
            for (i = 0; i < totalResource; i++)
            {

                GridResource res = createGridResource("Res_" + i , resourceProcessor[i],
                           totalMachine, resourceRating[i], resourcePrice[i], baud_rate, propDelay, mtu);

                // save resource ID sequentially 
                resInfoID[i] = res.get_id(); 
                // allocate this resource to a random regional GIS entity
                
                //gfaIndex = random.nextInt(num_GFA);
                gfaIndex = i;
                gfa = (RegionalGIS) gfaList.get(gfaIndex);
                res.setRegionalGIS(gfa);
                gfaInfo = (RegionalGFA) gfaInformationList.get(gfaIndex);
                gfaInfo.setGFAResourceMember(i);
                gfaInformationList.set(gfaIndex, gfaInfo);
                
                          

                // add a resource into a list
                resList.add(res);
                //resArray[i] = resName;
            }
            
            
            // Read gridWork Characteristics
            // number of Gridlets that will be sent to the resource
            int totalGridlet = 100;
            int   [] gridletNumber = new int  [totalGridlet];
            int   [] gridletSubmitTime = new int  [totalGridlet];
            int   [] gridletRunTime = new int  [totalGridlet];
            int   [] gridletDeadlineTime = new int  [totalGridlet];
            int   [] gridletProcessor = new int  [totalGridlet];
            double[] gridletLength = new double [totalGridlet];
            long file_size = 200;
            long output_size = 200;
            

            FileReader fr1 =new FileReader("gridWork.txt");            
            BufferedReader br1 =new BufferedReader(fr1);
            String line1;
            for (i = 0; i < 6; i++)
            {
                line1 = br1.readLine();
                String[] values1 = line1.split(",");
                if (i == 0)
                {
                    for (j = 0; j < totalGridlet; j++)   
                        gridletNumber[j]=Integer.parseInt(values1[j]);                    
                }
                else if (i == 1)
                {
                    for (j = 0; j < totalGridlet; j++)   
                        gridletSubmitTime[j]=Integer.parseInt(values1[j]);                    
                }
                else if (i == 2)
                {
                    for (j = 0; j < totalGridlet; j++)   
                        gridletRunTime[j]=Integer.parseInt(values1[j]);                    
                }
                else if (i == 3)
                {
                    for (j = 0; j < totalGridlet; j++)   
                        gridletDeadlineTime[j]=Integer.parseInt(values1[j]);                    
                }
                else if (i == 4)
                {
                    for (j = 0; j < totalGridlet; j++)   
                        gridletProcessor[j]=Integer.parseInt(values1[j]);                    
                }
                else if (i == 5)
                {
                    for (j = 0; j < totalGridlet; j++)   
                        gridletLength[j]=Integer.parseInt(values1[j]);                    
                }
                
            }
            br1.close();
            fr1.close();
            
            // Calculate End Time of simulation
            int approximateEndTime = gridletSubmitTime[totalGridlet - 1] + gridletRunTime[totalGridlet - 1] + 50;
            
            
            
            
            
/*
            //int totalGridlet = 250;
            //int   [] gridletVector = new int  [totalGridlet];
            //double[] gridletLength = new double [totalGridlet];
            int [] gridletNumber = {1,2,3,4,5}; // job number 
            int [] gridletProcessor = {4,30,27,88,26}; // number of processors
            //int [] gridletProcessor = {1,1,1,1,1}; // number of processors
            double [] gridletLength = {2353,2147,2356,7365,6508}; // job length
            int [] submitTime = {5,8,10,12,15}; // job submit time
            int [] runTime = {20,20,30,90,30}; // Job run time
            int [] deadlineTime = {30,70,40,100,40}; // Job deadline
            // int [] runTime = {20,60,30,90,30}; // Job run time
            //int [] deadlineTime = {30,70,40,100,40}; // Job deadline          
            long file_size = 200;
            long output_size = 200;
            //double gridletLength = 2000;
*/            
          
            /////////////////////////////////////////////
            // create users
            // with their gridWork set
            ArrayList userList = new ArrayList(num_user);
            for (i = 0; i < num_user; i++)
            {
   
                netuser10 user = new netuser10("User_" + i, baud_rate, propDelay, mtu, totalResource, 
                                gfaInformationList, totalGridlet, gridletNumber, gridletProcessor,
                                gridletSubmitTime, gridletRunTime, gridletDeadlineTime, file_size, output_size, gridletLength);
                
                
                user.setSimulationTime(approximateEndTime);
                user.setInfo(resInfoID);
                user.setGFAMap(adjacencyMatrix);
                gfaIndex = random.nextInt(num_GFA);
                //gfaIndex = 0;
                gfa = (RegionalGIS) gfaList.get(gfaIndex);
                user.setRegionalGIS(gfa); // set the regional GIS entity
                user.setLocalGFAIndex(gfaIndex);
                System.out.println(user.get_name() + " will communicate to " + gfa.get_name() +
                                                     " with id= " + gfa.get_id());
                // put this user into a list
                userList.add(user);
  
            }

            //////////////////////////////////////////
            // Step 6: Builds the network topology among entities.

            // In this example, the topology is:
            // user(s)     --1Gb/s-- r1 --10Gb/s-- r2 --1Gb/s-- GridResource(s)
            //                       |
            // workload(s) --1Gb/s-- |

            // create the routers.
            // If trace_flag is set to "true", then this experiment will create
            // the following files (apart from sim_trace and sim_report):
            // - router1_report.csv
            // - router2_report.csv
            Router r1 = new RIPRouter("router1", trace_flag);   // router 1
            Router r2 = new RIPRouter("router2", trace_flag);   // router 2
            Router r3 = new RIPRouter("router3", trace_flag);   // router 3

            // connect all user entities with r1 with 1Mb/s connection
            // For each host, specify which PacketScheduler entity to use.
            netuser10 userObj = null;
            for (i = 0; i < userList.size(); i++)
            {
                // A First In First Out Scheduler is being used here.
                // SCFQScheduler can be used for more fairness
                FIFOScheduler userSched = new FIFOScheduler("netuser10Sched_"+i);
                userObj = (netuser10) userList.get(i);
                r1.attachHost(userObj, userSched);
            }
/*                        
            // connect all Workload entities with r1 with 1Mb/s connection
            // For each host, specify which PacketScheduler entity to use.
            Workload w = null;
            for (i = 0; i < load.size(); i++)
            {
                // A First In First Out Scheduler is being used here.
                // SCFQScheduler can be used for more fairness
                FIFOScheduler loadSched = new FIFOScheduler("LoadSched_"+i);
                w = (Workload) load.get(i);
                System.out.println("Hint: " + w.getName());
                r1.attachHost(w, loadSched);
            }
*/
            // connect all resource entities with r2 with 1Mb/s connection
            // For each host, specify which PacketScheduler entity to use.
            GridResource resObj = null;
            for (i = 0; i < resList.size(); i++)
            {
                FIFOScheduler resSched = new FIFOScheduler("GridResSched_"+i);
                resObj = (GridResource) resList.get(i);
                r2.attachHost(resObj, resSched);
            }

            // then connect r1 to r2 with 10 Gbits/s connection
            // For each host, specify which PacketScheduler entity to use.
            baud_rate = 10000000;            
            
            Link link = new SimpleLink("r1_r2_link", baud_rate, propDelay, mtu);
            FIFOScheduler r1Sched = new FIFOScheduler("r1_Sched");
            FIFOScheduler r2Sched = new FIFOScheduler("r2_Sched");

            // attach r2 to r1
            r1.attachRouter(r2, link, r1Sched, r2Sched);

           // attach regional GFA entities to r3 router
            RegionalGIS gfaObj = null;
            for (i = 0; i < gfaList.size(); i++)
            {
                FIFOScheduler gfaSched = new FIFOScheduler("gfa_Sched" + i);
                gfaObj = (RegionalGIS) gfaList.get(i);
                r3.attachHost(gfaObj, gfaSched);
            }
            link = new SimpleLink("r2_r3_link", baud_rate, propDelay, mtu);
            FIFOScheduler r3Sched = new FIFOScheduler("r3_Sched");
            r2.attachRouter(r3, link, r2Sched, r3Sched);
            
            
            
            //////////////////////////////////////////
            // Step 7: Starts the simulation
            GridSim.startGridSimulation();

            //////////////////////////////////////////
            // Final step: Prints the Gridlets when simulation is over

            // also prints the routing table
            // r1.printRoutingTable();
            // r2.printRoutingTable();
            // r3.printRoutingTable();

            
            
            
            int c;
            //GridletList glList = null;
            for (i = 0; i < userList.size(); i++)
            {
                userObj = (netuser10) userList.get(i);     
                c = 0;
                for (j = 0; j < userObj.gridWorkNumber_; j++)
                {
                    if (userObj.gridWorkQueue_[j] == 2)
                        c++;
                }
                System.out.println("Hit_Query= " + (double) ((double)c / (double) userObj.gridWorkNumber_));
                System.out.println("Resource_Utilization= " + userObj.resourceUtil_);
                System.out.println("Resource_Income= " + userObj.resourceIncome_);
                System.out.println("User_Budjet_Remain= " + userObj.gridWorktotalBudjet_);
                System.out.println("User_Budjet_Total= " + (userObj.gridWorktotalBudjet_ + userObj.resourceIncome_));
                
            }
                    
/*            
            // prints the Gridlets inside a Workload entity
            for (i = 0; i < load.size(); i++)
            {
                w = (Workload) load.get(i);
                w.printGridletList(trace_flag);
            }
 
 */
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.out.println("Unwanted errors happen");
        }
    }

    /**
     * Creates one Grid resource. A Grid resource contains one or more
     * Machines. Similarly, a Machine contains one or more PEs (Processing
     * Elements or CPUs).
     * <p>
     * In this simple example, we are simulating one Grid resource with three
     * Machines that contains one or more PEs.
     * @param name          a Grid Resource name
     * @param baud_rate     the bandwidth of this entity
     * @param delay         the propagation delay
     * @param MTU           Maximum Transmission Unit
     * @param rating        a PE rating
     * @return a GridResource object
     */
    private static GridResource createGridResource
            (String name,int totalPE, int totalMachine, int rating,
             double price, double baud_rate, double delay, int MTU)
    {
        // create Machine list and add one or more machines to it
        MachineList mList = new MachineList();

        for (int i = 0; i < totalMachine; i++)
        {
            // Create one Machine with its id, number of PEs and rating
            mList.add( new Machine(i, totalPE, rating) );
        }

        // Create a ResourceCharacteristics object that stores the properties of a Grid resource:
        //  architecture, OS, list of Machines, number of PEs, rating of each PE
        // allocation policy: time- or space-shared, time zone and its price.
        
        String arch = "Intel";      // system architecture
        String os = "Linux";        // operating system
        double time_zone = 10.0;    // time zone this resource located

        // Create resource caracteristics
        ResourceCharacteristics resConfig = new ResourceCharacteristics(
                arch, os, mList, ResourceCharacteristics.SPACE_SHARED,
                time_zone, price);

        // Create a GridResource object.
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
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return gridRes;
    }    
    
    
    /**
     * Prints the Gridlet objects
     */
    private static void printGridletList(GridletList list, String name,
                                         boolean detail)
    {
        int size = list.size();
        Gridlet gridlet = null;

        String indent = "    ";
        System.out.println();
        System.out.println("============= OUTPUT for " + name + " ==========");
        System.out.println("Gridlet ID" + indent + "STATUS" + indent +
                "Resource ID" + indent + "Cost");

        // a loop to print the overall result
        int i = 0;
        for (i = 0; i < size; i++)
        {
            gridlet = (Gridlet) list.get(i);
            System.out.print(indent + gridlet.getGridletID() + indent
                    + indent);

            System.out.print( gridlet.getGridletStatusString() );

            System.out.println( indent + indent + gridlet.getResourceID() +
                    indent + indent + gridlet.getProcessingCost() );
        }

        if (detail == true)
        {
            // a loop to print each Gridlet's history
            for (i = 0; i < size; i++)
            {
                gridlet = (Gridlet) list.get(i);
                System.out.println( gridlet.getGridletHistory() );

                System.out.print("Gridlet #" + gridlet.getGridletID() );
                System.out.println(", length = " + gridlet.getGridletLength()
                        + ", finished so far = " +
                        gridlet.getGridletFinishedSoFar() );
                System.out.println("======================================\n");
            }
        }
    }
    
    
    public static int [] convertInt (ArrayList list)
    {
        int [] res = new int [list.size()];
        for (int i = 0; i < list.size(); i++)
        {
            res[i] = ((Integer) list.get(i)).intValue();
        }
        return res;
    }

} // end class

