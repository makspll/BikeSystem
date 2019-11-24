package uk.ac.ed.bikerental;

public class Location {
    private String postcode;
    private String address;
    
    public Location(String postcode, String address) {
        assert postcode.length() >= 6;
        this.postcode = postcode;
        this.address = address;
    }
    
    public boolean isNearTo(Location other) {
    	
    	String otherPostcode = other.postcode;
    	
        assert ( this.postcode != null       && otherPostcode != null       );
        assert ( this.postcode.length() >= 2 && otherPostcode.length() >= 2 );
        
        boolean nearEachOther = false;											// First, we set the return value to false. 
        
        if (this.postcode.charAt(0) == otherPostcode.charAt(0)) {
        	if (this.postcode.charAt(1) == otherPostcode.charAt(1)) {           // Because char is a primitive type, using "==" does the job.
        		nearEachOther = true;											// We set the return value to true, only if the first two chars match.
        	}
        }
        
        return nearEachOther;                                                   // We return the boolean value we computed.  
    }

    public String getPostcode() {
        return postcode;
    }

    public String getAddress() {
        return address;
    }
    
    // You can add your own methods here. 
    // But I don't think we have to. 
}
