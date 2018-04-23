package peer;


public class Triplet  implements Comparable<Object>{

	//peer transitivo é o peer que permite o caminho de credito entre provedor e consumidor
	private Peer consumer, transitivePeer;
	private double debt;	//débito do peer provedor, com o consumidor, via o peer transitivo
	
	public Triplet(Peer consumer, double debt, Peer transitivePeer){
		this.consumer = consumer;
		this.debt = debt;
		this.transitivePeer = transitivePeer;
	}
	
	@Override
	public boolean equals(Object o) {
		if(!(o instanceof Triplet))
			return false;
		else{
			Triplet otherTriplet = (Triplet) o;
			if(consumer != null && transitivePeer != null)
				return consumer.equals(otherTriplet.getConsumer()) && transitivePeer.equals(otherTriplet.getTransitivePeer());
			else if(consumer != null)
				return consumer.equals(otherTriplet.getConsumer()) && otherTriplet.getTransitivePeer()==null;
			else if(transitivePeer != null)
				return transitivePeer.equals(otherTriplet.getTransitivePeer()) && otherTriplet.getConsumer()==null;
			else return false;
		}
	}

	@Override
	public int compareTo(Object o) {
		final int BEFORE = -1;
	    final int EQUAL = 0;
	    final int AFTER = 1;
		
	    if(!(o instanceof Triplet))
			return EQUAL;
		
	    Triplet otherTriplet = (Triplet) o;
		
		if (debt < otherTriplet.getDebt()) 
	    	return BEFORE;
		else if(debt == otherTriplet.getDebt())
			return EQUAL;
		else	
	    	return AFTER;
	}
	
	@Override
	public String toString(){
		String output = "";
		if(consumer!=null)
			output = "ConsumerId: "+consumer.getId();
		if(transitivePeer!=null)
			output += "; TransitiveId: "+transitivePeer.getId();
		
		output += "; debt: "+debt;
		
		return output;
	}
	
	public Peer getConsumer() {
		return consumer;
	}
	
	public Peer getTransitivePeer() {
		return transitivePeer;
	}
	
	public double getDebt(){
		return debt;
	}
	
}
