package peer;

public class Quintuplet implements Comparable<Object>{

	//peer transitivo é o peer que permite o caminho de credito entre provedor e consumidor
	private Peer consumer, transitivePeer1, transitivePeer2, transitivePeer3;
	private double debt; //débito do peer provedor, com o consumidor, via o peer transitivo ????????? Saber se é o deb de B com a
		
	public Quintuplet(Peer consumer, double debt, Peer transitivePeer1, Peer transitivePeer2, Peer transitivePeer3){
		this.consumer = consumer;
		this.debt = debt;
		this.transitivePeer1 = transitivePeer1; // será o mais próximo do provedor
		this.transitivePeer2 = transitivePeer2;
		this.transitivePeer3 = transitivePeer3;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Quintuplet other = (Quintuplet) obj;
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
		if (transitivePeer3 == null) {
			if (other.transitivePeer3 != null)
				return false;
		} else if (!transitivePeer3.equals(other.transitivePeer3))
			return false;
		return true;
	}

	@Override
	public int compareTo(Object o) {
		final int BEFORE = -1;
	    final int EQUAL = 0;
	    final int AFTER = 1;
		
	    if(!(o instanceof Quintuplet))
			return EQUAL;
		
	    Quintuplet otherQuintuplet = (Quintuplet) o;
		
		if (debt < otherQuintuplet.getDebt()) 
	    	return BEFORE;
		else if(debt == otherQuintuplet.getDebt())
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
		if(transitivePeer3!=null)
			output += "; Transitive3Id: "+transitivePeer3.getId();
		
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
	
	public Peer getTransitivePeer3() {
		return transitivePeer3;
	}
	
	public double getDebt(){
		return debt;
	}
	
}
