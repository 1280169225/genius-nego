package borrowed;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import agents.HistorySpace;

import negotiator.AgentID;
import negotiator.Bid;
import negotiator.Deadline;
import negotiator.actions.Accept;
import negotiator.actions.Action;
import negotiator.actions.Offer;
import negotiator.parties.AbstractNegotiationParty;
import negotiator.session.TimeLineInfo;
import negotiator.utility.AbstractUtilitySpace;
import negotiator.boaframework.SortedOutcomeSpace;

/**
 * This is your negotiation party.
 */
public class MAS_assignment extends AbstractNegotiationParty {
	
	private SortedOutcomeSpace outcomeSpace;
	//private static double MINIMUM_BID_UTILITY = 0.0;
	private int start=1;//stop=-1;
	private double TargetBidDiff=0,MyOldUtility=1.0,threshold=0.0;
	private HashMap<AgentID,Double> Opponent_bid=new HashMap<AgentID,Double>();
	private HashMap<AgentID,Double> Opponent_difference=new HashMap<AgentID,Double>();
	
	public void init(AbstractUtilitySpace utilitySpace, Deadline deadlines, TimeLineInfo timeline, long randomSeed, AgentID id)
	{
		super.init(utilitySpace, deadlines, timeline, randomSeed, id);
		this.outcomeSpace = new SortedOutcomeSpace(this.utilitySpace);		
		threshold=utilitySpace.getReservationValueUndiscounted();
	}
	public Action chooseAction(List<Class<? extends Action>> validActions) {
		Action action = null;
		
		if ( !validActions.contains(Accept.class) || threshold < 0.7 ) {
			action=chooseOffer();
			MyOldUtility=getUtility(Action.getBidFromAction(action));
			return action;
		} else {
			return new Accept();
		}
	}
	
	public void receiveMessage(AgentID sender, Action action) {
		super.receiveMessage(sender, action);
		if(sender!=null){
			if(action==null)start=-1;
			else start=1;
			double Opp_OldBidVal=0;
			if(Opponent_bid.get(sender)!=null)
				Opp_OldBidVal=(double)Opponent_bid.get(sender);
			if(Opponent_bid.get(sender)!=null &&Opponent_bid.get(sender)!=getUtility(Action.getBidFromAction(action))){
				Opp_OldBidVal=Opponent_bid.get(sender);
				System.out.println(Opponent_bid.keySet());
				System.out.println(Opponent_bid.values());
				Opponent_bid.put(sender,(double)getUtility(Action.getBidFromAction(action)));
				double tempdiff=(getUtility(Action.getBidFromAction(action))-Opp_OldBidVal);
				tempdiff=Math.round(tempdiff*1000000.0)/1000000.0;
				Opponent_difference.put(sender,(getUtility(Action.getBidFromAction(action))-Opp_OldBidVal));
			}
			else if(Opponent_bid.get(sender)!=null&&Opponent_bid.get(sender)==getUtility(Action.getBidFromAction(action))){
				Opponent_difference.put(sender,(double) 0);
			}
			else 
			{
				Opponent_bid.put(sender,(double)getUtility(Action.getBidFromAction(action)));
				Opponent_difference.put(sender,0.0);
				System.out.println("case 3");
			}
			
			TargetBidDiff = Collections.min(Opponent_difference.values());
			threshold = Collections.min(Opponent_bid.values());
		}
	}
    
   
    public String getDescription() {
        return "MAS_Assignment";
    }
    
    private Action chooseOffer() 
	{
		Bid nextBid=null ;
		try {
			double utilityGoal;
			if(start!=-1){
					utilityGoal =MyOldUtility- TargetBidDiff;
			}
			else{
			//	System.out.println(My_currbid+" che");
					utilityGoal= MyOldUtility;
			}
			System.out.println(utilityGoal);
			nextBid = this.outcomeSpace.getBidNearUtility(utilityGoal).getBid();
		}
		catch (Exception e) { 
			System.out.println("Problem with received bid:"+e.getMessage()+". cancelling bidding"); 
			}
		if (nextBid == null) 
			return (new Accept());                
		return (new Offer(getPartyId(), nextBid));
	}
    
}

