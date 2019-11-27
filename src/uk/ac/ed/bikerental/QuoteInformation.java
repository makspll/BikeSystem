package uk.ac.ed.bikerental;

import uk.ac.ed.bikerental.Utils.ECollectionMode;

public class QuoteInformation
{
    public String name,surname,phone;
    public Location address;
    public ECollectionMode collectionMode;
    
    public QuoteInformation()
    {
        name = " ";
        surname = " ";
        phone = " ";
        address = new Location(" "," ");
        collectionMode = ECollectionMode.DELIVERY;
    }
    public QuoteInformation(Customer c, ECollectionMode pCollectionMode) {
    	name = c.getName();
    	surname = c.getSurname();
    	phone = c.getPhone();
    	address = c.getLocation();
    	collectionMode = pCollectionMode;
    }
}