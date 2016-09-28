package borrowed;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import negotiator.AgentID;
import negotiator.Bid;
import negotiator.Deadline;
import negotiator.actions.Accept;
import negotiator.actions.Action;
import negotiator.actions.Offer;
import negotiator.parties.AbstractNegotiationParty;
import negotiator.session.TimeLineInfo;
import negotiator.utility.AbstractUtilitySpace;
import negotiator.utility.UtilitySpace;
import negotiator.boaframework.SortedOutcomeSpace;

/**
 * This is your negotiation party.
 */
public class MAS_assignment extends AbstractNegotiationParty {
	
	private SortedOutcomeSpace outcomeSpace;
	private static double MINIMUM_BID_UTILITY = 0.0;
	private int start=1;//stop=-1;
	private double toBid_difference=0,My_prevbid=1.0,Acceptance_bid=1.0,threshold=0.0;
	private HashMap<Object,Double> Opponent_bid=new HashMap<Object,Double>();
	private HashMap<Object,Double> Opponent_difference=new HashMap<Object,Double>();
	
	public void init(AbstractUtilitySpace utilitySpace, Deadline deadlines, TimeLineInfo timeline, long randomSeed, AgentID id)
	{
		super.init(utilitySpace, deadlines, timeline, randomSeed, id);
		this.outcomeSpace = new SortedOutcomeSpace(this.utilitySpace);
		//System.out.println(this.outcomeSpace);
		//System.out.println("axiak");
		MINIMUM_BID_UTILITY = (utilitySpace).getReservationValueUndiscounted();
		threshold=MINIMUM_BID_UTILITY;
	}
	public Action chooseAction(List<Class<? extends Action>> validActions) {
		Action action = null;
		
		System.out.println(validActions.toString());
		if (  threshold < 0.7 ) {
			//return new Offer(generateRandomBid());
			action=chooseRandomBidAction();
			My_prevbid=getUtility(Action.getBidFromAction(action));
			return action;
		} else {
			return new Accept();
		}
	}
	
	public void receiveMessage(Object sender, Action action) {
		super.receiveMessage((AgentID) sender, action);
		// Here you hear other parties' messages
		if(sender.toString()!="Protocol"){
			if(action==null)start=-1;
			else start=1;
			double Opp_temprevBid=0;
			if(Opponent_bid.get(sender)!=null)
				Opp_temprevBid=(double)Opponent_bid.get(sender);
			
			if(Opponent_bid.get(sender)!=null &&Opponent_bid.get(sender)!=getUtility(Action.getBidFromAction(action))){
				Opp_temprevBid=Opponent_bid.get(sender);
				Opponent_bid.put(sender,(double)getUtility(Action.getBidFromAction(action)));
				double tempdiff=(getUtility(Action.getBidFromAction(action))-Opp_temprevBid);
				tempdiff=Math.round(tempdiff*1000000.0)/1000000.0;
				Opponent_difference.put(sender,(getUtility(Action.getBidFromAction(action))-Opp_temprevBid));
				System.out.println("case 1");
			}
			else if(Opponent_bid.get(sender)!=null&&Opponent_bid.get(sender)==getUtility(Action.getBidFromAction(action))){
				Opponent_difference.put(sender,(double) 0);
				System.out.println("case 2");
			}
			else 
			{
				Opponent_bid.put(sender,(double)getUtility(Action.getBidFromAction(action)));
				Opponent_difference.put(sender,0.0);
				System.out.println("case 3");
			}
			
			Double min = Collections.min(Opponent_difference.values());
			Double accept_min = Collections.min(Opponent_bid.values());
			threshold=accept_min;
			toBid_difference=min;
		}
	}
    
   
    public String getDescription() {
        return "MAS_Assignment";
    }
    
    public Bid pickBidOfUtility(double utility)
	  {
	    return this.outcomeSpace.getBidNearUtility(utility).getBid();
	  }
    
    private Action chooseRandomBidAction() 
	{
		Bid nextBid=null ;
		try {
			double utilityGoal;
			if(start!=-1){
					utilityGoal =My_prevbid-toBid_difference;
			}
			else{
			//	System.out.println(My_currbid+" che");
					utilityGoal= My_prevbid;
			}
			System.out.println(utilityGoal);
			nextBid = pickBidOfUtility(utilityGoal); }
		catch (Exception e) { System.out.println("Problem with received bid:"+e.getMessage()+". cancelling bidding"); }
		if (nextBid == null) return (new Accept());                
		return (new Offer(getPartyId(), nextBid));
	}
    
}

