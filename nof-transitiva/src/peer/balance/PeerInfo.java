package peer.balance;


public class PeerInfo implements Comparable<Object>{

	private int id;
	private double balance;	
	
	public PeerInfo(int id) {
		super();
		this.id = id;
		this.balance = 0;
	}
	
	/**
	 * The objects are equal if they have the same peerId.
	 */
	public boolean equals(Object obj) {
	       if (obj == null || !(obj instanceof PeerInfo))
	            return false;
	       else{
	    	   PeerInfo peerInfo = (PeerInfo) obj;
	    	   if(this.id == peerInfo.getId())
	    		   return true;
	    	   else
	    		   return false;
	       }
	}
	
	/**
	 * The list is sorted based on peers' balances.
	 */
	@Override
	public int compareTo(Object o) {
		final int BEFORE = -1;
	    final int EQUAL = 0;
	    final int AFTER = 1;
		
	    if(!(o instanceof PeerInfo))
			return EQUAL;
		
		PeerInfo otherPeer = (PeerInfo) o;
		
		if (this.balance < otherPeer.getBalance()) 
	    	return BEFORE;
		else if(this.balance == otherPeer.getBalance())
			return EQUAL;
		else	// (myBalance >= hisBalance) 
	    	return AFTER;
	}
	
	@Override
	public String toString(){
		return "Id: "+id+"; balance: "+balance;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public double getBalance() {
		return balance;
	}

	public void setBalance(double balance) {
		this.balance = balance;
	}

}