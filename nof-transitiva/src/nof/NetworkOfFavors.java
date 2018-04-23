package nof;

public class NetworkOfFavors {
	
	public static double calculateBalance(double consumedValue, double donatedValue){
		
		return Math.max(0, consumedValue - donatedValue);
	}
	
	public static double getFairness(double consumed, double donated){
		
		if(donated == 0)
			return -1;
		else
			return consumed/donated;
	}
	
	public static double getSatisfaction(double consumed, double requested){
		
		return 	getFairness(consumed, requested);
	}

}
