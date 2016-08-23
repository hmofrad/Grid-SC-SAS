package pricing;



/**
 *
 * @author MORBiD
 * Grid Federation Agent Class
 * The class consist additional application for GFAs.
 * This class work with Regional GIS class cooperatively.
 * 
 * 
 */

import java.util.ArrayList;

// Public class Grid Federation Agent (GFA)
public class RegionalGFA
{
    public String name_;                // My GFA name
    public int index_;                  // My GFA sequential index
    public int id_;                     // My GFA ID
    public ArrayList resourceList_;   // list of submitted resources to GFA corresponding cluster
    public double load_;
    public ArrayList Queue_;
     
    
    /** Creates a new GFA entity 
     * @param index this GIS index
     * @param id    this GIS id
     * @param name  this GIS name
     * @param Resource List  total number of Resources to be assigned
     * 
     */
     
     
     

    public RegionalGFA(String name, int ind, int id)
    {
        this.name_ = name;
        this.index_ = ind;
        this.id_ = id;
        this.resourceList_ = new ArrayList();
        this.load_ = 0.0;
        this.Queue_ = new ArrayList();
    }
    
    
    public int getGFAIndex()
    {
        return this.index_;
    }
    
    public int getGFAID()
    {
        return this.id_;
    }
    
    public ArrayList getGFAResourceList()
    {
        return this.resourceList_;
    }
    
    public void setGFAResourceMember(int memberResource)
    {
        this.resourceList_.add(memberResource);
    }
    
    

    
        
        
    
    
    

}
