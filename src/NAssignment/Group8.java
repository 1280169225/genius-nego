package NAssignment;

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
public class Group8 extends AbstractNegotiationParty {
	private SortedOutcomeSpace outcomeSpace;

	private double discountFactor = 0; // if you want to keep the discount
										// factor
	private double reservationValue = 0; // if you want to keep the reservation
											// value
	private double Given_util =0;
	private TimeLineInfo Tinfo = null;
	
	private Map<AgentID, Stack<Double>> offer_log = new HashMap<AgentID, Stack<Double> >();
	
	@Override
	public void init(AbstractUtilitySpace utilSpace, Deadline dl,
			TimeLineInfo tl, long randomSeed, AgentID agentId) {
		super.init(utilSpace, dl, tl, randomSeed, agentId);
		this.outcomeSpace = new SortedOutcomeSpace(this.utilitySpace);
		Tinfo = tl;
		utilitySpace.setDiscount(0.99);
		discountFactor = utilSpace.getDiscountFactor(); // read discount factor
		System.out.println("Discount Factor is " + discountFactor);
		utilSpace.setReservationValue(1.0);
		reservationValue = utilSpace.getReservationValueUndiscounted(); // read
																		// reservation
																		// value
		System.out.println("Reservation Value is =  " + reservationValue);

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
		System.out.print("rval = ");
		Double rv = this.utilitySpace.getReservationValueWithDiscount(Tinfo.getTime());
		System.out.println(rv);
		//Double rv = 0.5;
		if (!validActions.contains(Accept.class) || Given_util + 0.1 <=  rv) {
			Double best = 1.0;
			for(Stack<Double> element : offer_log.values()){
				Double x = element.peek();
				if(x>best)
					best = x;
			}
			if(best < rv){
				best = rv - (0.25 *(rv - best ) );
				best = rv;
			}
			Bid nextBid = this.outcomeSpace.getBidNearUtility(best).getBid();
			return new Offer(nextBid);
		} else {
			return new Accept();
		}
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
		if(sender!=null && action instanceof Offer){
			
			Given_util =  getUtilityWithDiscount(Action.getBidFromAction(action));
			//offer_log.put(sender, Given_util);
			if(!offer_log.containsKey(sender)){
				Stack<Double> st = new Stack<Double>();
				st.push(1.0);
				offer_log.put(sender, st);
			}
			Stack<Double> est = offer_log.get(sender);
			Double x = 0.0;
			do{
				x = est.pop();
				if(Given_util<= x){
					est.push(x);
					est.push(Given_util);
				}
			}while(Given_util > x);
			offer_log.put(sender, est);
		}
	}

	@Override
	public String getDescription() {
		return "Group8";
	}

}
