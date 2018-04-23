package peer;

public class Quadruplet implements Comparable<Object>{

	//peer transitivo é o peer que permite o caminho de credito entre provedor e consumidor
	private Peer consumer, transitivePeer1, transitivePeer2;
	private double debt; //débito do peer provedor, com o consumidor, via o peer transitivo ????????? Saber se é o deb de B com a
		
	public Quadruplet(Peer consumer, double debt, Peer transitivePeer1, Peer transitivePeer2){
		this.consumer = consumer;
		this.debt = debt;
		this.transitivePeer1 = transitivePeer1; // será o mais próximo do provedor
		this.transitivePeer2 = transitivePeer2;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Quadruplet other = (Quadruplet) obj;
		if (consumer == null) {
			if (other.consumer != null)
				return false;
		} else if (!consumer.equals(other.consumer))
			return false;
		if (transitivePeer1 == null) {
			if (other.transitivePeer1 != null)
				return false;
		} else if (!transitivePeer1.equals(other.transitivePeer1))
			return false;
		if (transitivePeer2 == null) {
			if (other.transitivePeer2 != null)
				return false;
		} else if (!transitivePeer2.equals(other.transitivePeer2))
			return false;
		return true;
	}




	@Override
	public int compareTo(Object o) {
		final int BEFORE = -1;
	    final int EQUAL = 0;
	    final int AFTER = 1;
		
	    if(!(o instanceof Quadruplet))
			return EQUAL;
		
	    Quadruplet otherQuadruplet = (Quadruplet) o;
		
		if (debt < otherQuadruplet.getDebt()) 
	    	return BEFORE;
		else if(debt == otherQuadruplet.getDebt())
			return EQUAL;
		else	
	    	return AFTER;
	}
	
	@Override
	public String toString(){
		String output = "";
		if(consumer!=null)
			output = "ConsumerId: "+consumer.getId();
		if(transitivePeer1!=null)
			output += "; Transitive1Id: "+transitivePeer1.getId();
		if(transitivePeer2!=null)
			output += "; Transitive2Id: "+transitivePeer2.getId();
		
		output += "; debt: "+debt;
		
		return output;
	}
	
	public Peer getConsumer() {
		return consumer;
	}
	
	public Peer getTransitivePeer1() {
		return transitivePeer1;
	}
	
	public Peer getTransitivePeer2() {
		return transitivePeer2;
	}
	
	public double getDebt(){
		return debt;
	}
	
}
