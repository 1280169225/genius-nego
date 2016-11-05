package NAssignment;

import java.util.List;
import java.util.*;

import negotiator.AgentID;
import negotiator.Bid;
import negotiator.Deadline;
import negotiator.actions.Accept;
import negotiator.actions.Action;
import negotiator.actions.Offer;
import negotiator.boaframework.SortedOutcomeSpace;
import negotiator.parties.AbstractNegotiationParty;
import negotiator.session.TimeLineInfo;
import negotiator.utility.AbstractUtilitySpace;

/**
 * This is your negotiation party.
 */
public class Kirk extends AbstractNegotiationParty {
	private SortedOutcomeSpace outcomeSpace;

	private double discountFactor = 0; // if you want to keep the discount
										// factor
	private double reservationValue = 0; // if you want to keep the reservation
											// value
	private double Given_util =0;
	
	private HashMap<AgentID, Double>  agentUtils = new HashMap<AgentID, Double>();
	
	private double n = 0, rounds = 0;
	
	private double prevUtil = 0;
	
	@Override
	public void init(AbstractUtilitySpace utilSpace, Deadline dl,
			TimeLineInfo tl, long randomSeed, AgentID agentId) {

		super.init(utilSpace, dl, tl, randomSeed, agentId);
		this.outcomeSpace = new SortedOutcomeSpace(this.utilitySpace);
		utilitySpace.setDiscount(0.99);
		discountFactor = utilSpace.getDiscountFactor(); // read discount factor
		System.out.println("Discount Factor is " + discountFactor);
		reservationValue = utilSpace.getReservationValueUndiscounted(); // read
																		// reservation
																		// value
		System.out.println("Reservation Value is " + reservationValue);

		// if you need to initialize some variables, please initialize them
		// below

	}

	/**
	 * Each round this method gets called and ask you to accept or offer. The
	 * first party in the first round is a bit different, it can only propose an
	 * offer.
	 *
	 * @param validActions
	 *            Either a list containing both accept and offer or only offer.
	 * @return The chosen action.
	 */
	// @Override
	public Action chooseAction(List<Class<? extends Action>> validActions) {
			Bid nextBid = null;
			double CurrUtil = 0;
			
			if(prevUtil == 0) {
				CurrUtil = 1;
			}
			
			else {
				Random r = new Random();
				int result = r.nextInt(100);
				
				if(result < ((n/rounds)*100)) {
					CurrUtil = prevUtil + getTimeLine().getTime()*0.01;
				}
				
				else {
					CurrUtil = prevUtil - getTimeLine().getTime()*0.01;
				}	
			}
			
			if(Given_util > CurrUtil) {
				System.out.println("here\n");
				return new Accept();
			}
			
			nextBid =  this.outcomeSpace.getBidNearUtility(CurrUtil).getBid();
			prevUtil = CurrUtil;
			
			return new Offer(nextBid);
	}

	/**
	 * All offers proposed by the other parties will be received as a message.
	 * You can use this information to your advantage, for example to predict
	 * their utility.
	 *
	 * @param sender
	 *            The party that did the action. Can be null.
	 * @param action
	 *            The action that party did.
	 */
	@Override
	public void receiveMessage(AgentID sender, Action action) {
		super.receiveMessage(sender, action);
		// Here you hear other parties' messages
//		System.out.println(getTimeLine().getTime());
		if(sender!=null && action !=null){
			Given_util =  getUtilityWithDiscount(Action.getBidFromAction(action));
			
			if(!agentUtils.containsKey(sender)) {
				agentUtils.put(sender, Given_util);
			}
			
			else {
				if((Double)agentUtils.get(sender).doubleValue() >= Given_util) {
					n++;
				}
				agentUtils.put(sender, Given_util);
			}
			
			Random r = new Random();
			int result = r.nextInt(100);
			
			if(result < 50) {
				n++;
			}
			
			rounds++;
		}
	}

	@Override
	public String getDescription() {
		return "Group8";
	}

}
