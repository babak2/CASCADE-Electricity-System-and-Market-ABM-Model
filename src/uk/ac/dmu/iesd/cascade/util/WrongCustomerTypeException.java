package uk.ac.dmu.iesd.cascade.util;

public class WrongCustomerTypeException extends RuntimeException{
	
/**
	 * 
	 */

public WrongCustomerTypeException() {
	System.err.println("Wrong type of customer found connected to aggregator");
	System.err.println("Check direction of edges in economic network etc.");
}

public WrongCustomerTypeException(Object cause){
	System.err.println("Wrong type of customer found connected to aggregator");
	System.err.println("Check direction of edges in economic network etc.");
	System.err.println(cause);
}
}
