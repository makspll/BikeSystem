package uk.ac.ed.bikerental;

public class PaymentHandler
{
	
	// This variable exists merely for ease of testing, and 
	// would not exist in the release version of the system
	public static boolean paymentHappened = false;
	
    public boolean payTo(String details)
    {
        //normally this would have i/o
        //we're assuming everyone pays correctly when this is called
    	PaymentHandler.paymentHappened = true;
        return true;
    }
}