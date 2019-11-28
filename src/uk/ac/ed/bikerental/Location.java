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
        
        if (this.postcode.substring(0,2).equals(otherPostcode.substring(0,2))) { //check the first 2 chars are the same
            return true;    //if so, we're near enough
        }
        else{
            return false;   //if not, we're not near enough
        }
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