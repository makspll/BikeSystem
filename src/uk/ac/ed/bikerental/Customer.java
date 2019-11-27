package uk.ac.ed.bikerental;

import java.util.List;
import java.util.ArrayList;
import uk.ac.ed.bikerental.Utils.EBikeType;
import uk.ac.ed.bikerental.Utils.ECollectionMode;
import uk.ac.ed.bikerental.BikeRentalSystem;

public class Customer
{
    private List<Invoice> currentInvoices;
    public BikeRentalSystem bikeSystem;
    
    private String name, surname, phone;
    private Location location;
    
    
    ///constructors
    public Customer(BikeRentalSystem brs, String name, String surname, String phone, String location) {
    	bikeSystem = brs;
    }

    ///getters
    public List<Invoice> getCurrentInvoices() {
    	return currentInvoices;
    }
    public Location getLocation() {return location;}
    public String getPhone() {return phone;}
    public String getName() {return name;}
    public String getSurname() {return surname;}
     
    ///setters
    public void setLocation(Location l) {location = l;}
    public void setPhone(String s) {phone = s;}
    public void setName(String s) {name = s;}
    public void setSurname(String s) {surname = s;}

    ///private parts

    public void removeInvoice (Invoice inv) {
    	for (Invoice i : currentInvoices) {
    		if (i.getOrderCode() == inv.getOrderCode()) {
    			currentInvoices.remove(i);
    		}
    	}
    }
    
    public List<Quote> findQuotes(DateRange dates,
                                    List<EBikeType> bikes,
                                    Location location)
    {
        List<Quote> quotesAvailable;
        try{
            quotesAvailable = bikeSystem.getQuotes(dates,bikes,location);
        }catch(Exception e)
        {
            quotesAvailable = null;
        }
        if(quotesAvailable != null && quotesAvailable.size() > 0)      // if we find some quotes
        {
            return quotesAvailable;
        }
        else        // if we cant find any quotes satisfying our needs , return empty list
        {
            List<Quote> empty = new ArrayList<Quote>(0);
            return empty;


        }
    }

    public boolean orderQuote(Quote q, ECollectionMode collectionMode)
    {
        //withing book quote the system prompts the user to pay
        Invoice invoice;
        QuoteInformation quoteInfo = new QuoteInformation(this, collectionMode);
        try{
            invoice = bikeSystem.bookQuote(q, quoteInfo);
        }catch(Exception e)
        {
            invoice = null;
        }

        if(invoice != null) //if the invoice is null, we cant book the quote, system state has changed 
        {
            currentInvoices.add(invoice);
            return true;
        }
        else
        {
            //we couldnt order the quote
            return false;
        }
    }

    // tells the system if the user needs a new selection of quotes,
    public boolean evaluateQuotes(List<Quote> quotesGiven)
    {
        //in our actual system, this would be where i/o happens, but
        //we were told not to care about that, so we don't.
        //in actual tests, we will just simulate the interaction without this function
        //but in the real system, IO would be called here
        return true;
    }

    public void returnBikeToOriginalProvider(int orderCode)
    {
        bikeSystem.recordReturnToOriginalProvider(orderCode);
    }
    public void returnBikeToPartnerProvider(int orderCode, int partnerId)
    {
        bikeSystem.recordBikeReturnToPartnerProvider(orderCode,partnerId);
    }

    
}