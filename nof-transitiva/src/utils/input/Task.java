package utils.input;


public class Task implements Comparable<Task>{
	
	private int runtime;
	
	public Task(int runtime){
		this.runtime = runtime;
	}

	public int getRuntime() {
		return runtime;
	}
	
	@Override
	public String toString() {
		return "runtime: "+runtime;
	}
	
	@Override
	public int compareTo(Task t) {
		final int BEFORE = -1;
	    final int EQUAL = 0;
	    final int AFTER = 1;
				
		if (runtime < t.getRuntime()) 
	    	return BEFORE;
		else if(runtime == t.getRuntime())
			return EQUAL;
		else	// (runtime >= t.runtime) 
	    	return AFTER;
	}

}