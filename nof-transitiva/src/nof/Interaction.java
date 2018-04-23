package nof;

import peer.Peer;

public class Interaction {
	
	private Peer peerB;
	private double consumed = 0, lastConsumed = 0;	//value that peer A consumed from peer B
	private double donated = 0, lastDonated = 0;	//value that peer A donated to peer B
	private final double initialCapacity;
	private double maxCapacityToSupply;
	private double capacitySuppliedHistory[];
	private boolean increasingCapacity;
	
	public Interaction(Peer peerB, double initialCapacity, int numSteps) {
		this.peerB = peerB;
		this.initialCapacity = maxCapacityToSupply = initialCapacity;
		this.capacitySuppliedHistory = new double[numSteps];
		this.capacitySuppliedHistory[0] = initialCapacity;
		this.increasingCapacity = false;
	}
	
	/**
	 * 
	 * @param donated value that peer A donated to peer B
	 */
	public void donate(double donated){
		
		this.donated += donated;
	}
	
	/**
	 * 
	 * @param consumed value that peer A consumed from peer B
	 */
	public void consume(double consumed){
		this.consumed += consumed;
	}
	
	public void saveLastValues(){
		lastDonated = donated;
		lastConsumed = consumed;
	}
	
	public boolean equals(Object obj) {
	    if (obj == null || !(obj instanceof Interaction))
	         return false;
	    else{
	 	   Interaction i = (Interaction) obj;
	 	   if(this.peerB.getId() == i.getPeerB().getId())
	 		   return true;
	 	   else 
	 		   return false;
	    }
	}

	public Peer getPeerB() {
		return peerB;
	}

	public void setPeerB(Peer peerB) {
		this.peerB = peerB;
	}

	public double getMaxCapacityToSupply() {
		return maxCapacityToSupply;
	}

	public void setMaxCapacityToSupply(double maxCapacityToSupply) {
		this.maxCapacityToSupply = maxCapacityToSupply;
	}

	public double[] getCapacitySuppliedHistory() {
		return capacitySuppliedHistory;
	}

	public boolean isIncreasingCapacity() {
		return increasingCapacity;
	}

	public void setIncreasingCapacity(boolean increasingCapacity) {
		this.increasingCapacity = increasingCapacity;
	}

	public double getConsumed() {
		return consumed;
	}

	public double getDonated() {
		return donated;
	}

	public double getLastConsumed() {
		return lastConsumed;
	}

	public double getLastDonated() {
		return lastDonated;
	}

	public double getInitialCapacity() {
		return initialCapacity;
	}	
	
	public void makeDonationEqualToZero(){
		lastDonated = donated = 0;
	}
}