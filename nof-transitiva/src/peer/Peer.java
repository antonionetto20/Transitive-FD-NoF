package peer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import nof.Interaction;
import peer.balance.PeerInfo;

public class Peer implements Comparable<Peer>{
	
	protected final double INITIAL_CAPACITY; 
	protected double demand, resourcesDonatedInCurrentStep;
	protected int id;
	protected ArrayList<PeerInfo> balances; // aqui estão os credores e devedores
	protected ArrayList <Interaction> interactions;
	private double maxCapacityToSupply;
	private boolean increasingCapacitySupplied;
	private double consumedHistory[], donatedHistory[], requestedHistory[], capacitySuppliedHistory[];
//	private double consumedByTransitivityHistory[], donatedByTransitivityHistory[], requestedHistory[], capacitySuppliedHistory[];
	private PeerState state;
	
	public Peer(int id, double initialCapacity, int numSteps) {
		super();
		this.INITIAL_CAPACITY = initialCapacity;
		this.id = id;
		this.resourcesDonatedInCurrentStep = 0;
		this.balances = new ArrayList<PeerInfo>();
		this.interactions = new ArrayList<Interaction>();
		this.consumedHistory = new double[numSteps];
		this.donatedHistory = new double[numSteps];
		this.requestedHistory = new double[numSteps];
		this.capacitySuppliedHistory = new double[numSteps];		
		this.setIncreasingCapacitySupplied(false);					
		this.setMaxCapacityToSupply(initialCapacity);		
	}
	
	public PeerState getState(){
		return state;
	}
	
	public void setState(PeerState state) {
		this.state = state;
	}
	
	public int hashCode() {
        return this.id;
    }
	
	public double getInitialCapacity() {
		return INITIAL_CAPACITY;
	}
	
	public int getId(){
		return this.id;
	}
	
	public void setId(int id){
		this.id = id;
	}
	
	public double getDemand() {
		return demand;
	}
	
	public void setDemand(double demand) {
		this.demand = demand;
	}	
	
	public double getResourcesDonatedInCurrentStep() {
		return resourcesDonatedInCurrentStep;
	}
	
	public void setResourcesDonatedInCurrentStep(double resourcesDonatedInCurrentStep) {
		this.resourcesDonatedInCurrentStep = resourcesDonatedInCurrentStep;
	}
	
	public ArrayList<PeerInfo> getBalances() {
		return balances;
	}

	public void setPeersReputations(ArrayList<PeerInfo> peersReputations) {
		this.balances = peersReputations;
	}
	
	/**
	 * The peer with highest balance might already been used. Therefore, we will seek
	 * the higher value before this. Where attempt = 2, means the second higher value in
	 * the SortedList, and so on.
	 * 
	 * @param nth the nth highest balance
	 * @return the peer id with the nth highest balance
	 */
	public int getThePeerIdWithNthBestReputation(int nth){
		if(nth<=0)
			return -1;
		
		//sort the balance
		Collections.sort(balances);		
		for(int i = this.balances.size()-1; i>=0 ;i--){
			if(nth==1)
				return this.balances.get(i).getId();
			nth--;
		}
		
		return -1;
	}	
	
	public List<Interaction> getInteractions() {
		return interactions;
	}

	private void setInteractions(ArrayList<Interaction> interactions) {
		this.interactions = interactions;
	}
	
	public double[] getConsumedHistory() {
		return consumedHistory;
	}
	
	public double getCurrentConsumed(int step) {
		double currrentConsumed = 0;
		for(int i = 0; i <= step; i++)
			currrentConsumed += consumedHistory[i];
		return currrentConsumed;
	}
	
	public double getCurrentConsumed(int beginning, int end) {
		double currrentConsumed = 0;
		for(int i = beginning; i <= end; i++)
			currrentConsumed += consumedHistory[i];
		return currrentConsumed;
	}
	
//	public double[] getConsumedByTransitivityHistory() {
//		return consumedByTransitivityHistory;
//	}
//	
//	public double getCurrentConsumedByTransitivity(int step) {
//		double currrentConsumed = 0;
//		for(int i = 0; i <= step; i++)
//			currrentConsumed += consumedByTransitivityHistory[i];
//		return currrentConsumed;
//	}
//	
//	public double getCurrentConsumedByTransitivity(int beginning, int end) {
//		double currrentConsumed = 0;
//		for(int i = beginning; i <= end; i++)
//			currrentConsumed += consumedByTransitivityHistory[i];
//		return currrentConsumed;
//	}
	
	public double[] getDonatedHistory() {
		return donatedHistory;
	}
	
	public double getCurrentDonated(int step) {
		double currrentDonated = 0;
		for(int i = 0; i <= step; i++)
			currrentDonated += donatedHistory[i];
		return currrentDonated;
	}
	
	public double getCurrentDonated(int beginning, int end) {
		double currrentDonated = 0;
		for(int i = beginning; i <= end; i++)
			currrentDonated += donatedHistory[i];
		return currrentDonated;
	}
	
//	public double[] getDonatedByTransitivityHistory() {
//		return donatedByTransitivityHistory;
//	}
//	
//	public double getCurrentDonatedByTransitivity(int step) {
//		double currrentDonated = 0;
//		for(int i = 0; i <= step; i++)
//			currrentDonated += donatedByTransitivityHistory[i];
//		return currrentDonated;
//	}
//	
//	public double getCurrentDonatedByTransitivity(int beginning, int end) {
//		double currrentDonated = 0;
//		for(int i = beginning; i <= end; i++)
//			currrentDonated += donatedByTransitivityHistory[i];
//		return currrentDonated;
//	}
	
	public double [] getRequestedHistory() {
		return requestedHistory;
	}
	
	public double getCurrentRequested(int step) {
		double currrentRequested = 0;
		for(int i = 0; i <= step; i++)
			currrentRequested += requestedHistory[i];
		return currrentRequested;
	}
	
	public double[] getCapacitySuppliedHistory() {
		return capacitySuppliedHistory;
	}

	public double getINITIAL_CAPACITY() {
		return INITIAL_CAPACITY;
	}
	
	public double getMaxCapacityToSupply() {
		return maxCapacityToSupply;
	}
	
	public void setMaxCapacityToSupply(double maxCapacityToSupply) {
		this.maxCapacityToSupply = maxCapacityToSupply;
	}
	
	public boolean isIncreasingCapacitySupplied() {
		return increasingCapacitySupplied;
	}

	public void setIncreasingCapacitySupplied(boolean increasingCapacitySupplied) {
		this.increasingCapacitySupplied = increasingCapacitySupplied;
	}	
	
	public boolean equals(Object obj) {
	       if (obj == null || !(obj instanceof Peer))
	            return false;
	       else{
	    	   Peer p = (Peer) obj;
	    	   if(this.id == p.getId())
	    		   return true;
	    	   else
	    		   return false;
	       }
	}

	@Override
	public String toString() {
		return " Id = " + id + ", " + "Peer INITIAL_CAPACITY = " + INITIAL_CAPACITY + ", Demand = " + demand + ", ResourcesDonatedInCurrentStep = "
				+ resourcesDonatedInCurrentStep + "\n";
	}

	@Override
	public int compareTo(Peer p) {
		if(p.getId() > getId())
			return -1;
		else if(p.getId() < getId())
			return 1;
		else
			return 0;
	}
}