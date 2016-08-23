/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pricing;

/**
 *
 * @author MORBiD
 */

import gridsim.Gridlet;
import gridsim.GridletList;
import java.util.LinkedList;

public class gridWorkload extends Gridlet

{
    int gridletSubmitTime_;
    int gridletRunTime_;
    int gridletdeadlineTime_;
    int gridletprocessorNum_;

    

    
    
    public gridWorkload(int userID, int gridletNumber, int gridletProcessor,
                        int gridletSumbitTime, int gridletRunTime, int gridletdeadlineTime, long file_size, long output_size,
                        double gridletLength)
  
    {
        super(gridletNumber, gridletLength, file_size,output_size);
            //super.setNumPE(gridletProcessor);
            super.setUserID(userID);
            
            this.gridletSubmitTime_ = gridletSumbitTime;
            this.gridletRunTime_ = gridletRunTime;
            this.gridletdeadlineTime_ = gridletdeadlineTime;
            this.gridletprocessorNum_ = gridletProcessor;
            
            // System.out.println("Hint_JobID: " + super.getGridletID());
            // System.out.println("Hint_UserID: " + super.getUserID());
            // System.out.println("Hint_NumPE: " + super.getNumPE());
        
    }
        
}
