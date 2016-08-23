package pricing;

/**
 *
 * @author MORBiD
 * a test example for dynamic pricing 
 * using super-scheduling algorithm 
 * for computational grids
 * Greedy Backfill Super Scheduling
 */

import java.util.*;
import gridsim.*;
import gridsim.net.*;
import gridsim.util.*;
import java.io.IOException;
import gridsim.GridSim;
import gridsim.datagrid.DataGridUser;
import gridsim.net.SimpleLink;
import eduni.simjava.*;
import gridsim.index.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Arrays;




/**
 * This class basically creates Gridlets and submits them to a 
 * particular GridResources in a network topology.
 */
class netuser11 extends GridUser
{
    private int myId_;      // my entity ID
    private String name_;   // my entity name
    private GridletList list_;          // list of submitted Gridlets
    private GridletList receiveList_;   // list of received Gridlets

    
    private LinkedList <gridWorkload> gridWorkList_;
    private LinkedList <gridWorkload> recievegridletlist_;
    
    private int [] resourceInfoID;
    private ArrayList GFAInfoList;
    private int myGFAIndex_;
    private int [][] GFAMap; 
    private int approximateEnd_;
    private SimReport report_;  // logs every events
    
    public int successReq;
    public int[] gridletStatus;
    public int[] hopReq;
    public double resourceUtil_;
    public double resourceIncome_;
    
    public double gridWorktotalBudjet_;
    private double []  gridWorkLength_;
    public int  gridWorkNumber_;
    
    
    private ArrayList gridWorkSubmit_;
    private ArrayList gridWorkRun_;
    private ArrayList gridWorkDeadline_;
    private ArrayList gridWorkProcessor_;
    public int [] gridWorkQueue_;
    // private int  gridWorkActivated_;
    private ArrayList  gridWorkActivated_;
    private ArrayList  gridWorkRemain_;
    




    


    /**
     * Creates a new NetUser object
     * @param name  this entity name
     * @param totalGridlet  total number of Gridlets to be created
     * @param baud_rate     bandwidth of this entity
     * @param delay         propagation delay
     * @param MTU           Maximum Transmission Unit
     * @throws Exception    This happens when name is null or haven't
     *                   
     */
    netuser11(String name, double baud_rate, double delay, int MTU,int totalResource, ArrayList GFAList,
            int totalGridlet, int [] gridletNumber, int [] gridletProcessor, int [] submitTime,
            int [] runTime, int [] deadlineTime, long file_size, long output_size, double [] gridletLength) throws Exception
    {
        super( name, new SimpleLink(name+"_link",baud_rate,delay, MTU) );

        int i;
        this.name_ = name;
        this.receiveList_ = new GridletList();
        this.list_ = new GridletList();


        // Gets an ID for this entity
        this.myId_ = super.getEntityId(name);
        System.out.println("Creating a grid user entity with name = " + name + ", and id = " + this.myId_);
        
        
        this.gridWorkLength_ = new double[totalGridlet];
        for (i = 0; i < totalGridlet; i++)
            this.gridWorkLength_[i] = 0;
        
        this.gridWorkList_ =  new LinkedList();
        // Creates a list of Gridlets or Tasks for this grid user
        System.out.println(name + ":Creating " + totalGridlet +" Gridlets");
        for (i = 0; i < totalGridlet; i++)
        {
            gridWorkload gridWork = new gridWorkload(this.myId_, gridletNumber[i], gridletProcessor[i],
                                submitTime[i], runTime[i], deadlineTime[i], file_size, output_size, gridletLength[i]);

            this.gridWorkList_.add(gridWork);
            this.gridWorktotalBudjet_ +=  gridletLength[i] ;             // Caculate user budjet
            //System.out.println("Hint_len" + gridletLength[i]);
            this.gridWorkLength_[i] = gridletLength[i];
            //System.out.println("Hint_len" +this.gridworkLength_[i] );
        }
        this.gridWorkNumber_ = totalGridlet;

        
        //this.createGridlet(myId_, totalGridlet, gridletLength, gridletPEVector);
        
        
        
        
        this.resourceInfoID = new int [totalResource];
        this.GFAInfoList = GFAList;
        
        this.resourceUtil_ = 0.0;
        this.resourceIncome_ = 0.0;
        
        this.hopReq = new int[totalGridlet];
        for (i = 0; i < totalGridlet; i++)
            this.hopReq[i] = 0;
        
        this.gridletStatus = new int[totalGridlet];
        for (i = 0; i < totalGridlet; i++)
            this.gridletStatus[i] = 0;

        this.successReq = 0;
        
        
        
        this.gridWorkSubmit_ = new ArrayList(); // gridWork submit time array
        this.gridWorkRun_ = new ArrayList(); // gridWork Run time array
        this.gridWorkDeadline_ = new ArrayList(); // gridWork deadline time array
        this.gridWorkProcessor_ = new ArrayList(); // gridWork deadline time array
        
        // Super-Scheduler Queue
        
        //-3 as Loading   gridWork
        //-2 as Arrived   gridWork
        //-1 as submited  gridWork
        // 0 as Waiting   gridWork
        // 1 as Executing gridWork
        // 2 as finished  gridWork
        // 3 as droped    gridwork
        
        
        this.gridWorkQueue_ = new int [totalGridlet];
        for (i = 0; i < totalGridlet; i++)
            this.gridWorkQueue_[i] = -3;
        //this.gridWorkActivated_ = 0;
        this.gridWorkActivated_ = new ArrayList();
        this.gridWorkRemain_ = new ArrayList();

        
    }

    /**
     * The core method that handles communications among GridSim entities.
     */
    public void body()
    {
        
    super.gridSimHold(3000.0*5);    // wait for time in seconds
        LinkedList resList = super.getGridResourceList();
        System.out.println(resList.size());
        System.out.println();
        int i = 0;
        int j =0;
        int k = 0;

         // initialises all the containers
        int totalResource = resList.size();
        int resourceID[] = new int[totalResource];
        String resourceName[] = new String[totalResource];
        // a loop to get all the resources available

        for (i = 0; i < totalResource; i++)
        {
            resourceID[i] = this.resourceInfoID[i];
            resourceName[i] = GridSim.getEntityName(resourceID[i]);
        }
        
        ResourceCharacteristics resChar;
        ArrayList resCharList = new ArrayList(totalResource);
        for (i = 0; i < totalResource; i++)
        {            
            // Requests to resource entity to send its characteristics
            super.send(resourceID[i], GridSimTags.SCHEDULE_NOW
                                    , GridSimTags.RESOURCE_CHARACTERISTICS,this.myId_);
            // waiting to get a resource characteristics
            resChar = (ResourceCharacteristics) super.receiveEventObject();

            resourceName[i] = resChar.getResourceName();
            
            //System.out.println( " " +  " " + resChar.getMachineList().getMachine(0).getNumFreePE());
            resCharList.add(resChar);
/*      
            System.out.println("Receiving ResourceCharacteristics from " + resourceName[i] +
                               ", with id = " + resourceID[i] + ", and PE = "
                               + resChar.getMachineList().getMachine(0).getNumPE()
                               +  ", and Price = " + resChar.getCostPerSec());
            
*/
        }
        System.out.println(); 

        
        // Sync Network Topology
        int [] GFAAdjacency;
        int [][] adjacencyMatrix = this.GFAMap;
/*        
        // Print Network Topology
        for (i = 0; i < adjacencyMatrix.length; i++)
        {
            for (j = 0; j < adjacencyMatrix.length; j++)
                System.out.print(adjacencyMatrix[i][j] + " ");
            System.out.println();
        }
*/ 
        

 


        // initialize resource price
        int num_GFA = totalResource;
        double [] resourcePrice = new double [totalResource];
        double [] resourceLoad = new double [totalResource];
        int [] resourceSentinel = new int [totalResource];
        RegionalGFA GFA_obj;  
        int memberRsource;
        double resourceMaxLoad = 0.85;
        double resourceLoadFactor = 0.3;
        double resourceLoadWeight = 0.1;
        int [] resourceProcessor = new int [totalResource];
        ArrayList[] resourceQueue = new ArrayList[totalResource];
        for (i = 0; i < totalResource; i++)
            resourceQueue[i] = new ArrayList();
        int [] resourceBusy = new int [totalResource];
        int [] resourceBusyTemp = new int [totalResource];
        double [] resourceProbability = new double [totalResource];
        int [] resourceRatioPlus = new int [totalResource];
        int [] resourceRatioNeg = new int [totalResource];
        double [] resourceLoadNormal = new double [totalResource];
        double resourceSpeedRatio = 0;
        int [] resourceVisit = new int [totalResource];
        int resourceIncome = 0;
        
        
        for (i = 0; i <num_GFA ; i++)
        {
            // each GFA has one member resource
            GFA_obj = (RegionalGFA) GFAInfoList.get(i);
            memberRsource = ((Integer) GFA_obj.resourceList_.get(0)).intValue();
            // System.out.println("Hint_member: " + memberRsource[j]);
            resChar = (ResourceCharacteristics) resCharList.get(memberRsource);
            String str = resChar.getResourceName();
            int indStr = str.indexOf('_') + 1;
            String numStr = str.substring(indStr);
            int resourceIndex = Integer.parseInt(numStr);
            // initialize resource price
            resourcePrice[resourceIndex] = resChar.getCostPerSec(); // 1D --> each GFA has one Resource;
            resourceProcessor[resourceIndex] = resChar.getNumPE();
            //dynamicPrice[i] = resChar.getCostPerSec(); // 1D --> each GFA has one Resource
            //System.out.println("Hint_Initial_Price: " + resourcePrice[intStr]);
            // initilize resource load
            resourceLoad[resourceIndex] = 0.0;
            resourceSentinel[resourceIndex] = 0;
            resourceProbability[resourceIndex] = 0;
            resourceRatioPlus[resourceIndex] = 0;
            resourceRatioNeg[resourceIndex] = 0;
            resourceLoadNormal[resourceIndex] = 0;
            resourceVisit[resourceIndex] = 0;
            
            //System.out.println("Hint_D_load: " + resourceLoad[intStr]);
        }
        
        ////////////////////////////////////////////////
        // Determine GFA status
        // update resource price based on current load, overload
        // and user budjet
        ArrayList[] gfaTrace = new ArrayList[this.gridWorkNumber_];  // 1D array
        for (i = 0; i < this.gridWorkNumber_; i++)
            gfaTrace[i] = new ArrayList();
        
        //gfaTrace.add(this.myGFAIndex_); 
        int [] localGFAIndex = new int [this.gridWorkNumber_];
        for (i = 0; i < this.gridWorkNumber_; i++)
                localGFAIndex[i] = this.myGFAIndex_;
        
        int [] localGFA = new int [this.gridWorkNumber_];
            for (i = 0; i < this.gridWorkNumber_; i++)
                localGFA[i] = localGFAIndex[i];
        
        for (i = 0; i < this.gridWorkNumber_; i++)
                localGFA[i] = this.myGFAIndex_;;        
        
        int [][] isVisitGFA = new int [this.gridWorkNumber_][num_GFA];
        for (i = 0; i < this.gridWorkNumber_; i++)
            for (j = 0; j < num_GFA; j++)
                isVisitGFA[i][j] = 0;
        
        int [] sumisVisitGFA = new int [this.gridWorkNumber_];
            for (i = 0; i < this.gridWorkNumber_; i++)
                sumisVisitGFA[i] = 0;

        int totalGridlet = this.gridWorkList_.size(); 
        //int gridWorkNumber; // number of gridWork
        
        // superscheduler Queue parameters
        // ArrayList gridWorkSubmit = new ArrayList(); // gridWork submit time array
        // ArrayList gridWorkRun = new ArrayList(); // gridWork Run time array
        // ArrayList gridWorkDeadline = new ArrayList(); // gridWork deadline time array
        
        //////////////////////////////
        // SLA bid negotiation Phase
        // tick the Grid clock
        i = 0; // gridletIndex                
        // Time parameters
        int globalTime = -1;
        int endTime = this.approximateEnd_;
        int gridWorkIndex;
        int gridWorkExpire;
        int gridWorkIndexTemp;
        int gridWorkRemain;
        int gridWorkMaxHop = 99;
        int gridWorkStat;
        double gridWorkOutcome= 0.0;
        
        Random random = new Random();   // a random generato
        

        
        // Sync global Time till first submitted job arrive
        if (this.gridWorkList_.get(i).gridletSubmitTime_ > globalTime)
                    globalTime = this.gridWorkList_.get(i).gridletSubmitTime_;
        //System.out.println("Hint_globalTime: " +  globalTime); 
        // Check if the new job arrive
        i = gridWorkNotify(globalTime, i);
        //System.out.println("Hint_global_Time: " +  globalTime);
        //System.out.println("Hint_Submit_Time: " +  this.gridWorkList_.get(0).gridletSubmitTime_);
        //System.out.println("Hint_gridWork_Index: " +  i);
        for (j = 0; j < this.gridWorkNumber_; j++)
            System.out.print(this.gridWorkQueue_[j] + " ");
        System.out.println();
        
        
        // Static or Dynamic Price
        // 0 as Static price
        // 1 as Dynamic Price
        int resourcePriceMode = 1;

        // Strategy Mode Selection  
        // 0 as Adaptive Super Scheduling
        // 1 as greedy Super Scheduling
        int schedulerStrategy = 1;
        int resourceCharTemp;
        int resourceCharMax;
        int resourceCharMaxIndex;
        
        
        
        
        ///////////////////////////////
        // main loop for job echeduling
        // globalTime will tick per each GFA
        // each GFA has one resource
        // each user has set of gridWork
        // SuperScheduler has one queue 
        while (globalTime < endTime)
        {
      

            ////////////////////////////////////////////////
            // SUBMIT gridWokrs by user
            // determines which GridResource to send 
            // Caculate user budjet for each gridWork
            // gridWorkNumber = this.gridWorkList_.size();
            double [] gridWorkBudjet = new double [this.gridWorkNumber_];
            for (j = 0; j < this.gridWorkNumber_; j++)
            {
                if (this.gridWorkQueue_[j] < 0)
                //{
                    gridWorkBudjet[j] = (this.gridWorkLength_[j] / this.gridWorktotalBudjet_) * this.gridWorktotalBudjet_;
                //System.out.println("Hint_Total: " + this.gridWorktotalBudjet_);
                //System.out.println("Hint_Budjet_" + j +": " + gridWorkBudjet[j]);
                //}
            }
            
            
            // User Side
            // for all submmited gridWorks do the for loop
            // Query the status of Queue
            this.gridWorkActivated_.clear();
            //System.out.println("Hint_Activated_Clear: " + gridWorkActivated_.size());
            for (j = 0; j < this.gridWorkNumber_; j++)
            {
                if (-2 <= this.gridWorkQueue_[j]  && this.gridWorkQueue_[j] < 2) 
                {
                    this.gridWorkActivated_.add(j);
                    //System.out.println("Hint_Activated_ " +  j + ": "  + this.gridWorkQueue_[j]);
                    //System.out.println("Hint_Activated_ " + ((Integer) this.gridWorkActivated_.get(this.gridWorkActivated_.size()-1)).intValue());
                }
            }
/*            
            // check if all work finished
            if (this.gridWorkActivated_.isEmpty())
            {
                System.out.println("Hint_Queue_empty___Break_" + globalTime);
                break;
            }
*/            
            
            
            for (j = 0; j < totalResource; j++)
                resourceSentinel[j] = 0;
            
            // Core loop to handel gridWork
            System.out.println(globalTime +" ==> " + "Queue_Size:" + this.gridWorkActivated_.size());
            for (j = 0; j < this.gridWorkActivated_.size(); j++)
            {
                
                // Current gridWork Index
                gridWorkIndex  = ((Integer) this.gridWorkActivated_.get(j)).intValue();
                gridWorkExpire = ((Integer) this.gridWorkDeadline_.get(gridWorkIndex)).intValue() + 
                                 ((Integer) this.gridWorkSubmit_.get(gridWorkIndex)).intValue();
                // System.out.println("Hint_gridWork_" + gridWorkIndex + "_Expires_" + gridWorkExpire);

                // System.out.println("Hint_Index: " + gridWorkIndex);
                
                
                // Determine the local GFA peer Neighbors
                GFAAdjacency = extractGFAAdjacency(adjacencyMatrix, localGFAIndex[gridWorkIndex]);
                // Sort GFA peers
                //Arrays.sort(GFAAdjacency);
                // find GFA with Max Throughput
                resourceCharTemp = 0;
                resourceCharMax = 0;
                resourceCharMaxIndex = 0;
                for (k = 0; k < GFAAdjacency.length; k++)
                {
                    GFA_obj = (RegionalGFA) GFAInfoList.get(GFAAdjacency[k]);
                    memberRsource = ((Integer) GFA_obj.resourceList_.get(0)).intValue();
                    resChar = (ResourceCharacteristics) resCharList.get(memberRsource);
                    resourceCharTemp = resChar.getNumPE();
                    //System.out.println(resourceCharTemp);
                    if (resourceCharTemp > resourceCharMax)
                    {
                        resourceCharMax = resourceCharTemp;
                        resourceCharMaxIndex = k;
                    }
                }
                System.out.println(resourceCharMaxIndex + " "+ resourceCharMax);
                
                // set local GFA index for user                
                localGFA[gridWorkIndex] = localGFAIndex[gridWorkIndex]; // GFA index from 0 to num_GFA
                gfaTrace[gridWorkIndex].add(localGFA[gridWorkIndex]); // save trace of super-scheduler
                //System.out.println("Hint_j: " + this.gridWorkActivated_);
                
                
                /////////////////////////////////////
                // update all member resources  price 
                // of local GFA based on resource load
                GFA_obj = (RegionalGFA) GFAInfoList.get(localGFA[gridWorkIndex]);
                memberRsource = ((Integer) GFA_obj.resourceList_.get(0)).intValue();
                resChar = (ResourceCharacteristics) resCharList.get(memberRsource);
                String str = resChar.getResourceName();
                int indStr = str.indexOf('_') + 1;
                String numStr = str.substring(indStr);
                int resourceIndex = Integer.parseInt(numStr);

                /////////////////////////////////////////////
                // if resource load is more than maxload and
                // if user budjet is more than resource price
                // increase resource price
                // System.out.println("Hint_resource_Price_"  + resourceIndex  + ": " + resourcePrice[resourceIndex]); 
                // check the mode first
                
                if (resourcePriceMode == 1)
                {
                    if (this.gridWorkQueue_[gridWorkIndex] < 0)
                    {
                        if (resourceLoad[resourceIndex] >= resourceMaxLoad)
                        {
                            if (gridWorkBudjet[gridWorkIndex] > resourcePrice[resourceIndex])
                            {
                                //System.out.println("Hint_before_Price: " + resourcePrice[resourceIndex]);
                                resourcePrice[resourceIndex] = resourcePrice[resourceIndex]
                                                             + (resourceLoadWeight * resourcePrice[resourceIndex]);
                                //System.out.println("Hint_after_Price: " + resourcePrice[resourceIndex]);
                            }


                            // if resource load is less than maxload and
                            // if user budjet is less than resource price
                            // increase resource price
                        }
                        else
                        {
                        //    if (this.gridWorkQueue)
                        //    {

                        //    }
                            if (gridWorkBudjet[gridWorkIndex] < resourcePrice[resourceIndex])

                            {
                                //System.out.println("Hint_before_Price: " + resourcePrice[resourceIndex]);
                                resourcePrice[resourceIndex] = resourcePrice[resourceIndex]
                                                             - (resourceLoadWeight * resourcePrice[resourceIndex]);
                                //System.out.println("Hint_after_Price: " + resourcePrice[resourceIndex]);
                            }

                            // Local Resource Management Service (LRMS)
                            // LRMS sort member resource of local GFA
                            // as long as each GFA has one resource 
                            // resource processor array will contain this information
                            // resChar = (ResourceCharacteristics) resCharList.get(memberRsource);
                            // resourceProcessor[resourceIndex] = resChar.getNumPE();
                            // System.out.println("Hint_PE: " + resourceProcessor[resourceIndex]);   
                        }
                        // esle if
                        // keep current price
                        /////////////////////////////////////////////
                    }
                }
                
                
                                

                
                
                
/*                
                // check status of current gridWork
                if (this.gridWorkQueue[gridWorkIndex] == 0)
                {
                    System.out.println("Hint_Queue_" + gridWorkIndex + "_: " + this.gridWorkQueue[gridWorkIndex]); 
                    
                }
 

                in
*/                
                // System.out.println("Hint_gridWork_Budjet_" + gridWorkIndex  + ": " + gridWorkBudjet[gridWorkIndex]);
                // System.out.println("Hint_resource_Price_"  + resourceIndex  + ": " + resourcePrice[resourceIndex]);
                // Impelement Super-Schedluing Via Switch Statement
                gridWorkStat = this.gridWorkQueue_[gridWorkIndex];
                switch (gridWorkStat)
                {
                    /////////////////////////////
                    case -2: //Arrived gridWork
                    {          
                        
                        


                        /////////////////////////////////////////////
                        // check if user has sufficient budjet to run
                        // the current gridWork
                        // the  first match will choose for job scheduling
                        // as long as each GFA has one resource
                        if (gridWorkBudjet[gridWorkIndex] >= resourcePrice[resourceIndex])
                        {
                            // submit job into GFA queue
                            // as long as we have one resource per GFA
                            // we update resource Queue
                            System.out.println("Hint_Resource_Index_" + gridWorkIndex + ": " + resourceIndex);
                            // System.out.println("Hint_gridWork_Index_" + gridWorkIndex);
                            //System.out.println("Hint_gridWork_Status_" + gridWorkIndex + ": "  +  this.gridWorkQueue_[gridWorkIndex]);
                            resourceQueue[resourceIndex].add(gridWorkIndex);
                            // update superscheduler queue status
                            // 0 = submmited gridWork
                            this.gridWorkQueue_[gridWorkIndex] = -1;
                            System.out.println("Hint_gridWork_Status_Change_" + gridWorkIndex + ": " +  this.gridWorkQueue_[gridWorkIndex]);
                            resourceRatioPlus[resourceIndex]++;
                            resourceVisit[resourceIndex] = 1;

                        }
                        else if (gridWorkBudjet[gridWorkIndex] < resourcePrice[resourceIndex])
                        {
                            // price of the current GFA
                            // is more than user budjet
                            // super scheduler sumbit the current
                            // gridWork into another GFA

                            // System.out.println("Hint_gridWork_Forward_" + gridWorkIndex);
                            // System.out.println("Hint_PreviousGFA_" + gridWorkIndex + ": " + localGFAIndex[gridWorkIndex] );
                            // localGFAIndex[gridWorkIndex] = (localGFAIndex[gridWorkIndex] + 1) % num_GFA;
                            // System.out.println("Hint_NextGFA_" + gridWorkIndex + ": " + localGFAIndex[gridWorkIndex] );
                            if ( schedulerStrategy == 0)
                                localGFAIndex[gridWorkIndex] = random.nextInt(GFAAdjacency.length);
                             else if (schedulerStrategy == 1)
                                localGFAIndex[gridWorkIndex] = GFAAdjacency[resourceCharMaxIndex];

                            resourceRatioNeg[resourceIndex]++;
                        }
                        break;
                    } // End of Arrived gridWork
                    
                    /////////////////////////////
                    case -1: // Submited gridWork
                    {   
                       for (k=0; k < resourceQueue[resourceIndex].size(); k++)
                       {   
                           gridWorkIndexTemp = ((Integer) resourceQueue[resourceIndex].get(k)).intValue();
                           if (gridWorkIndexTemp == gridWorkIndex)
                           {
                               // if the submitted gridWork is the first submmited gridWork of resource queue
                               if (k == 0)
                               {
                                   System.out.println("Hint_1st_GridWork_" + gridWorkIndex + ": " + resourceBusy[resourceIndex]);


                                    System.out.println( gridWorkIndex + " " + resourceIndex + " " +
                                                      ((Integer) this.gridWorkDeadline_.get(gridWorkIndex)).intValue() + " " +
                                                      ((Integer) this.gridWorkSubmit_.get(gridWorkIndex)).intValue()  + " " +
                                                      ((Integer) this.gridWorkRun_.get(gridWorkIndex)).intValue()  + " " +
                                                      ((Integer) this.gridWorkProcessor_.get(gridWorkIndex)).intValue()  + " " +
                                                        globalTime + " " +  resourceProcessor[resourceIndex]);


                                    resourceSpeedRatio = (double) ((double) resourceProcessor[resourceIndex] /
                                                     (double) ((Integer) this.gridWorkProcessor_.get(gridWorkIndex)).intValue());
                                    gridWorkRemain = (int) Math.ceil( (double) (((Integer) this.gridWorkDeadline_.get(gridWorkIndex)).intValue()
                                                   + ((Integer) this.gridWorkSubmit_.get(gridWorkIndex)).intValue()
                                                   - globalTime)
                                                   * resourceSpeedRatio);
                                    //resourceBusy[resourceIndex] = globalTime + gridWorkRemain;
                                    System.out.println("Hint_Remain_" + gridWorkIndex +": " + gridWorkRemain);
/*                                    
                                    System.out.println("Hint_Ratio_" + gridWorkIndex + "_" + resourceIndex +
                                            "_" + ((Integer) this.gridWorkProcessor_.get(gridWorkIndex)).intValue() +
                                            "_"  + resourceProcessor[resourceIndex] + "_" +
                                            ((double) resourceProcessor[resourceIndex] /
                                            (double) ((Integer) this.gridWorkProcessor_.get(gridWorkIndex)).intValue()));
*/


                                    //System.out.println("Hint_Remain_Double_"+ gridWorkIndex + ": " + gridWorkRemain);
                                    //System.out.println("Hint_Remain_Int_"+ gridWorkIndex + ": " + resourceBusy[resourceIndex]);
                                    //System.out.println("Hint_globalTime:" + globalTime );
                                    // if the gridWork meet the condition of execution
                                    // so change its status into running 
                                    if (gridWorkRemain >= ((Integer) this.gridWorkRun_.get(gridWorkIndex)).intValue())
                                    {

                                        // calculate Running Time
                                        // assign the job to the resource
                                        // change job status to executing 1
                                        // change resource status to busy for the Running Time
                                        System.out.println("Hint_Submit_for_executing");
                                        // goto loop for the two following ADDs
                                        resourceBusy[resourceIndex] = (int) Math.ceil((double) ((Integer) this.gridWorkRun_.get(gridWorkIndex)).intValue() 
                                                                    / resourceSpeedRatio);
                                        System.out.println("Hint_Remain_Calc_"+ gridWorkIndex + ": " + resourceBusy[resourceIndex]);


                                        this.gridWorkRemain_.set(gridWorkIndex, resourceBusy[resourceIndex] + globalTime + 1);
                                        //System.out.println("Hint_globalTime: " + globalTime + " Hint_Remain: " +  
                                        //                    ((Double) this.gridWorkRemain_.get(gridWorkIndex)).doubleValue());
                                        // change gridWork status into executing
                                        // in the Next time stamp
                                        this.gridWorkQueue_[gridWorkIndex] = 0;
                                        // Update totalGridWorkBudjet
                                        this.gridWorktotalBudjet_  -= resourcePrice[resourceIndex];
                                                   resourceIncome  += resourcePrice[resourceIndex];
                                    }

                                    // if the job can not successfully execute in the current resource
                                    // reject the SLA bid 
                                    // call the super-scheduler to choose next GFA
                                    else
                                    {
                                        // change to status of gridWork into arrived
                                        this.gridWorkQueue_[gridWorkIndex] = -2;
                                        // remove the gridWork from resource queue
                                        resourceQueue[resourceIndex].remove(k);
                                        // System.out.println("Hint_PreviousGFA_" + gridWorkIndex + ": " + localGFAIndex[gridWorkIndex] );
                                        // Super-Scheduler select a new GFA to assign the gridWork
                                        //localGFAIndex[gridWorkIndex] = (localGFAIndex[gridWorkIndex] + 1) % num_GFA;
                                        // System.out.println("Hint_NextGFA_" + gridWorkIndex + ": " + localGFAIndex[gridWorkIndex] );
                                        resourceRatioNeg[resourceIndex]++;
                                        if ( schedulerStrategy == 0)
                                            localGFAIndex[gridWorkIndex] = random.nextInt(GFAAdjacency.length);
                                         else if (schedulerStrategy == 1)
                                            localGFAIndex[gridWorkIndex] = GFAAdjacency[resourceCharMaxIndex];

                                    }


                                } // End of if (k == 0)

                                // if the submitted gridWork is not the first submmited gridWork of resource queue
                                else
                                {

                                    // Process Next gridWork till first job 
                                    // which assigned to this resource queue is finished
                                    System.out.println("Hint_2nd_GridWork_" + gridWorkIndex + ": " + resourceBusy[resourceIndex]);

                                    System.out.println( gridWorkIndex + " " + resourceIndex + " " +
                                                      ((Integer) this.gridWorkDeadline_.get(gridWorkIndex)).intValue() + " " +
                                                      ((Integer) this.gridWorkSubmit_.get(gridWorkIndex)).intValue()  + " " +
                                                      ((Integer) this.gridWorkRun_.get(gridWorkIndex)).intValue()  + " " +
                                                        globalTime + " " +
                                                        resourceProcessor[resourceIndex]);



                                    resourceSpeedRatio = (double) ((double) resourceProcessor[resourceIndex] /
                                                     (double) ((Integer) this.gridWorkProcessor_.get(gridWorkIndex)).intValue());
                                    gridWorkRemain = (int) Math.ceil( (double) (((Integer) this.gridWorkDeadline_.get(gridWorkIndex)).intValue()
                                                   + ((Integer) this.gridWorkSubmit_.get(gridWorkIndex)).intValue()
                                                   - globalTime)
                                                   * resourceSpeedRatio);
                                    
                                    //resourceBusy[resourceIndex] = globalTime + gridWorkRemain;
                                    System.out.println("Hint_Remain_" + gridWorkIndex +": " + gridWorkRemain);

                                    if (gridWorkRemain >= ((Integer) this.gridWorkRun_.get(gridWorkIndex)).intValue())
                                    {

                                        // calculate Running Time
                                        // assign the job to the resource
                                        // change job status to executing 1
                                        // change resource status to busy for the Running Time
                                        System.out.println("Hint_Submit_to_" + resourceIndex);
                                        // goto loop for the two following ADDs
                                        // add the run time to gridWork Queue
                                        //resourceBusyTemp[resourceIndex] = resourceBusy[resourceIndex];
                                        // this.gridWorkRemain_.set
                                        resourceBusyTemp[resourceIndex] = (int) Math.ceil((double) ((Integer) this.gridWorkRun_.get(gridWorkIndex)).intValue() 
                                                                    / resourceSpeedRatio);
                                        System.out.println("Hint_Remain_Time:" + resourceBusyTemp[resourceIndex]);
                                        
                                        
                                        System.out.println("Original_" + resourceBusy[resourceIndex] + " Temp_" + resourceBusyTemp[resourceIndex]);
                                        // if the work could still schedule for execution in currrent queue
                                        if (resourceBusy[resourceIndex] + resourceBusyTemp[resourceIndex] + globalTime + 1 <= gridWorkExpire)
                                        {
                                            System.out.println("Queue_Enabaled");
                                            resourceBusy[resourceIndex] += resourceBusyTemp[resourceIndex];
                                            this.gridWorkRemain_.set(gridWorkIndex, resourceBusy[resourceIndex] + globalTime + 1);
                                            //System.out.println("Hint_globalTime: " + globalTime + " Hint_Remain: " +  
                                            //                    ((Double) this.gridWorkRemain_.get(gridWorkIndex)).doubleValue());
                                            this.gridWorkQueue_[gridWorkIndex] = 1; // gridWork Queue wait 

                                            // Update totalGridWorkBudjet
                                            this.gridWorktotalBudjet_ -= resourcePrice[resourceIndex];
                                                       resourceIncome += resourcePrice[resourceIndex];

                                        }

                                        // if the gridWork will drop by maintaining its position in the current queue
                                        else
                                        {

                                            // change to status of gridWork into arrived
                                            this.gridWorkQueue_[gridWorkIndex] = -2;
                                            // remove the gridWork from resource queue
                                            resourceQueue[resourceIndex].remove(k);
                                            // System.out.println("Hint_PreviousGFA_" + gridWorkIndex + ": " + localGFAIndex[gridWorkIndex] );
                                            // localGFAIndex[gridWorkIndex] = (localGFAIndex[gridWorkIndex] + 1) % num_GFA;
                                            // System.out.println("Hint_NextGFA_" + gridWorkIndex + ": " + localGFAIndex[gridWorkIndex] );
                                            resourceRatioNeg[resourceIndex]++;
                                            if ( schedulerStrategy == 0)
                                                localGFAIndex[gridWorkIndex] = random.nextInt(GFAAdjacency.length);
                                            else if (schedulerStrategy == 1)
                                                localGFAIndex[gridWorkIndex] = GFAAdjacency[resourceCharMaxIndex];

                                        }
                                    } // End of if ( >= )

                                    // if the gridWork can not successfully execute in the current resource
                                    // reject the SLA bid 
                                    // call the super-scheduler to choose next GFA
                                    else
                                    {
                                        // change to status of gridWork into arrived
                                        this.gridWorkQueue_[gridWorkIndex] = -2;
                                        // remove the gridWork from resource queue
                                        resourceQueue[resourceIndex].remove(k);
                                        // System.out.println("Hint_PreviousGFA_" + gridWorkIndex + ": " + localGFAIndex[gridWorkIndex] );
                                        // localGFAIndex[gridWorkIndex] = (localGFAIndex[gridWorkIndex] + 1) % num_GFA;
                                        // System.out.println("Hint_NextGFA_" + gridWorkIndex + ": " + localGFAIndex[gridWorkIndex] );
                                        resourceRatioNeg[resourceIndex]++;
                                        if ( schedulerStrategy == 0)
                                            localGFAIndex[gridWorkIndex] = random.nextInt(GFAAdjacency.length);
                                        else if (schedulerStrategy == 1)
                                            localGFAIndex[gridWorkIndex] = GFAAdjacency[resourceCharMaxIndex];
                                    }
                                    
                                } // End of else
                               
                           } //End of if (gridWorkIndexTemp == gridWorkIndex)
                           
                       } // End of for()
                       
                        break;
                    } // End of Submited gridWork
                    
                    /////////////////////////////
                    case 0: // Executing gridWork
                    {          
                        
                        // if the first job in the queue is  related to this gridWork
                        // and its status is executing
                        // check if its finished or not?
                        // System.out.println("Hint_gridWork_Status_" + gridWorkIndex + ": " + this.gridWorkQueue_[gridWorkIndex]);
                        // check the running status of work
                        // if the work is finished 
                        // change its status to finished 2
                        //(Integer) this.gridWorkRemain_.get(gridWorkIndex)).intValue())
                        //System.out.println("Hint_globalTime: " + globalTime + " Hint_Remain: " +  
                        //                        ((Double) this.gridWorkRemain_.get(gridWorkIndex)).doubleValue());
                        resourceBusy[resourceIndex]--;
                        System.out.println("Hint_Resource_Busy_" + gridWorkIndex+ ": " + resourceBusy[resourceIndex]);
                        System.out.println("Hint_gridWork_Remain_" + gridWorkIndex+ ": " + ((Integer) this.gridWorkRemain_.get(gridWorkIndex)).intValue());
                        // if the time has come
                        // remove the current gridWork from gridWork queue
                        if (globalTime == ((Integer) this.gridWorkRemain_.get(gridWorkIndex)).intValue())
                        {
                            System.out.println("Hint_gridWork_" + gridWorkIndex + "= Finished!!!");
                            this.gridWorkQueue_[gridWorkIndex] = 2;
                            // System.out.println("Hint_RQ_before_ " + resourceQueue[resourceIndex].size());
                            resourceQueue[resourceIndex].remove(0);
                            // System.out.println("Hint_RQ_after: " + resourceQueue[resourceIndex].size());
                        }                        
                        
                        break;
                    }
                    /////////////////////////////
                    case 1: // Waiting gridWork
                    {
                        // if the waiting gridWork is the first in the queue
                        // change its status to execute in the time stamp
                        
                        if (((Integer) resourceQueue[resourceIndex].get(0)).intValue() == gridWorkIndex)
                        {
                            this.gridWorkQueue_[gridWorkIndex] = 0;
                            System.out.println(((Integer) resourceQueue[resourceIndex].get(0)).intValue());
                            System.out.println("hint_get_out_of_queue_" + gridWorkIndex);
                            
                        }
                        
                        
                        
                        break;
                    }
                    /////////////////////////////
                    default:
                    {
                        
                        
                       // do nothing

                        break;
                    }
                
                } // End of Switch()
                
                
                //////////////////////////////////////////                
                // Resouce side
                // calculate resource load
                // based on summited gridWork into it
                if (resourceSentinel[resourceIndex] == 0)
                {
                    // Calculate Probability
                    resourceProbability[resourceIndex] = (double)  resourceRatioPlus[resourceIndex] / 
                                                         (double) (resourceRatioPlus[resourceIndex] + 
                                                                  resourceRatioNeg[resourceIndex]);
                    System.out.println("Hint_Prob_ " + resourceIndex + ": " + resourceProbability[resourceIndex]);
                    // Calculate standard resource load
                    resourceLoad[resourceIndex] = resourceLoadFactor * resourceQueue[resourceIndex].size();
                    // Calculate Normalized Load
                    resourceLoadNormal[resourceIndex] = resourceLoad[resourceIndex] + (resourceLoad[resourceIndex] * resourceProbability[resourceIndex]);
                    resourceLoad[resourceIndex] = resourceLoadNormal[resourceIndex];
                    System.out.println("Hint_Load_ " + resourceIndex + ": " + resourceLoad[resourceIndex]);
                    resourceSentinel[resourceIndex] = 1;
                    
                }
                
                
                // if the summitted gridWork Expire
                // drop it from activated gridWork Queue
                if (globalTime >  gridWorkExpire)
                {                    
                    System.out.println("Hint_S+D_ " + gridWorkIndex + ": " + (((Integer) this.gridWorkDeadline_.get(gridWorkIndex)).intValue() + 
                    ((Integer) this.gridWorkSubmit_.get(gridWorkIndex)).intValue()));
                    
                    System.out.println("Hint_R_" + ((Integer) this.gridWorkRemain_.get(gridWorkIndex)).intValue());
                    System.out.println("Hint_Drop_gridWork_" + gridWorkIndex);
                    this.gridWorkQueue_[gridWorkIndex] = 3;    
                }
                
/*               
                // if the gridWork max GFA check
                if (gfaTrace[gridWorkIndex].size() > gridWorkMaxHop)
                {
                    gridWorkQueue_[gridWorkIndex] = 3;
                    System.out.println("Hint_"+ gfaTrace[gridWorkIndex].size() + " Drop_ " + this.gridWorkQueue_[gridWorkIndex]);

                }
*/
                
            } // End of For (this.gridWorkActivated_.size())
            
                
            
            
            
                
            
            
            // Increase time Stamp
            // show status of gridWodk
            globalTime++;
            i = gridWorkNotify(globalTime, i);    
/*            
            System.out.print(globalTime + " ==> ");
            for (k = 0; k < this.gridWorkNumber_; k++)
                System.out.print(gridWorkQueue_[k] + " ");
            System.out.println();
*/            

        } // End of While
        
        
        // Print gridWork status
        for (i = 0; i < this.gridWorkNumber_; i++)
            System.out.print(gridWorkQueue_[i] + " ");
        System.out.println();
        
        int c = 0;
        for(i = 0; i < totalResource; i++)
        {
            if (resourceVisit[i] == 1)
                c++;
        }
        this.resourceUtil_ = (double) ((double) c / (double) totalResource );
        this.resourceIncome_  = resourceIncome;
        

        
        

        
                    


                
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        


        
        
        
/*        
        int index = myId_ % totalResource;
        if (index >= totalResource) {
            index = 0;
        }

        // sends all the Gridlets
         Gridlet gl = null;
        //Gridlet g1 = this.gridletlist_.get(0);  
        //System.out.println("Hint_Len:" + g1.getGridletLength());
        
        boolean success;
        for (i = 0; i < this.gridWorkList_.size(); i++)
        {
            gl = (Gridlet) this.gridWorkList_.get(i);
            write(this.name_ + "Sending Gridlet #" + i + " to " + resourceName[index]);
            
            // For even number of Gridlets, send without an acknowledgement
            // whether a resource has received them or not.
            if (i % 2 == 0)
            {
                // by default - send without an ack
                success = super.gridletSubmit(gl, resourceID[index]);                
            }

            // For odd number of Gridlets, send with an acknowledgement
            else
            {
                // this is a blocking call
                success = super.gridletSubmit(gl,resourceID[index],0.0,true);
                write("ack = " + success + " for Gridlet #" + i);
            }
        }

        ////////////////////////////////////////////////////////
        // RECEIVES Gridlets back

        // hold for few period - few seconds since the Gridlets length are
        // quite huge for a small bandwidth
        super.gridSimHold(5);
        // Gridlet gl = null;
        // receives the gridlet back
        for (i = 0; i < totalGridlet; i++)
        {
            gl = (Gridlet) super.receiveEventObject();  // gets the Gridlet
            receiveList_.add(gl);   // add into the received list

            write(name_ + ": Receiving Gridlet #" +
                  gl.getGridletID() + " at time = " + GridSim.clock() );
        }
 */
/*
        ////////////////////////////////////////////////////////
        // ping functionality
        InfoPacket pkt = null;
        int size = 500;

        // There are 2 ways to ping an entity:
        // a. non-blocking call, i.e.
        //super.ping(resourceID[index], size);    // (i)   ping
        //super.gridSimHold(10);        // (ii)  do something else
        //pkt = super.getPingResult();  // (iii) get the result back

        // b. blocking call, i.e. ping and wait for a result
        pkt = super.pingBlockingCall(resourceID[index], size);       

        // print the result
        write("\n-------- " + name_ + " ----------------");
        write(pkt.toString());
        write("-------- " + name_ + " ----------------\n");
*/        
        
        

        ////////////////////////////////////////////////////////
        // shut down I/O ports
        shutdownUserEntity();
        terminateIOEntities();

        // don't forget to close the file
        if (report_ != null) {
            report_.finalWrite();
        }

        write(this.name_ + ": sending and receiving of Gridlets" +
              " complete at " + GridSim.clock() );
    }

    /**
     * Gets a list of received Gridlets
     * @return a list of received/completed Gridlets
     */
    public GridletList getGridletList() 
    {
        return receiveList_;
    }
    
    /**
     * Prints the Gridlet objects
     * @param detail    whether to print each Gridlet history or not
     */
    public void printGridletList(boolean detail)
    {
        LinkedList list = receiveList_;
        String name = name_;

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

    /**
     * This method will show you how to create Gridlets
     * @param userID        owner ID of a Gridlet
     * @param numGridlet    number of Gridlet to be created
     */
    private void createGridlet(int userID, int numGridlet)
    {
        int data = 5000;   // 5 MB of data
        for (int i = 0; i < numGridlet; i++)
        {
            // Creates a Gridlet
            Gridlet gl = new Gridlet(i, data, data, data);
            gl.setUserID(userID);

            // add this gridlet into a list
            this.list_.add(gl);
        }
    }

    /**
     * Prints out the given message into stdout.
     * In addition, writes it into a file.
     * @param msg   a message
     */
    private void write(String msg)
    {
        System.out.println(msg);
        if (report_ != null) {
            report_.write(msg);
        }
    }
    
    private void createGridlet(int userID, int numGridlet, double [] gridletLength, int [] gridletPE)
    {
        double length;
        long file_size = 300;
        long output_size = 300;
        for (int i = 0; i < numGridlet; i++) // i as Gridlet ID
        {
            // Creates a Gridlet
            length = gridletLength[i];
            Gridlet gl = new Gridlet(i,length , file_size, output_size);
            gl.setNumPE(gridletPE[i]);
            gl.setUserID(userID);
            // add this gridlet into a list
            this.list_.add(gl);

        }

    }
    
    
    public void setInfo(int resInfoID [])
    {
        this.resourceInfoID = resInfoID; // list of Grid Resources
    }
    
    
    public void setLocalGFAIndex(int index)
    {
        this.myGFAIndex_ = index;
    }
    
    
    public void setGFAMap(int adjacencyMatrix [][])
    {
        this.GFAMap = adjacencyMatrix; // GIS network Topology
    }

    
    public void setSimulationTime(int endTime)
    {
        this.approximateEnd_ = endTime; // GIS network Topology
    }
    
    
    public int [] extractGFAAdjacency (int [][] array, int index)
    {
        int adjacenceGFANumel = 0;
        int i = 0;
        for (i = 0; i < array.length; i++)
        {
            if (array[index][i] == 1)
                adjacenceGFANumel++;
        }
        int [] adjacenceGFAIndex = new int [adjacenceGFANumel];
        int c = 0;
        for (i = 0; i < array.length; i++)
        {
            if (array[index][i] == 1)
            {
                adjacenceGFAIndex[c] = i;
                //System.out.print(adjacenceGFAIndex[c] + " ");
                c++;
            }
        }
        //System.out.println();
        return adjacenceGFAIndex;

    }
    
    

    
    
    public int gridWorkNotify (int globalTime, int i)
    {
        
        // if we have more than 1 submmited job change condition
        if (i < this.gridWorkNumber_)
        {
            if (globalTime >= this.gridWorkList_.get(i).gridletSubmitTime_)
            {
                System.out.println("Hint_New Job Recieved at time: " + globalTime);   
                this.gridWorkSubmit_.add(this.gridWorkList_.get(i).gridletSubmitTime_);
                this.gridWorkRun_.add(this.gridWorkList_.get(i).gridletRunTime_);
                this.gridWorkDeadline_.add(this.gridWorkList_.get(i).gridletdeadlineTime_);
                this.gridWorkProcessor_.add(this.gridWorkList_.get(i).gridletprocessorNum_);
                
                this.gridWorkRemain_.add(0);
                this.gridWorkQueue_[i] = -2;     
                i++;
                //this.gridWorkActivated_++;
                //this.gridWorkActivated_.add(i);
                //if (i ==  this.gridWorkNumber_)
                    //i--;
            }
        }
        
        return i;
    }
    
    
    
    
    

} // end class

