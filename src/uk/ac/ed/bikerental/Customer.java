package uk.ac.ed.bikerental;

import java.utils.List;


public class customer
{
    private List<Invoice> currentInvoices;
    public BikeRentalSystem bikeSystem;
    ///constructors
    

    ///getters
     

    ///private parts

    public List<Quote> findQuotes(DateRange dates,
                                    List<Bike> bikes,
                                    Location location)
    {
        List<Quote> quotesAvailable = bikeSystem.getQuotes(dates,bikes,location);
        if(quotesAvailable.size() > 0)      // if we find some quotes
        {
            return quotesAvailable;
            //this would be displayed to the user here
        }
        else        // if we cant find any quotes satisfying our needs , return empty list
        {
            return new List<Quote>(0);
            //in the normal system, this would be displayed to the user

        }
    }

    public boolean orderQuote(Quote q, Utils.QuoteInformation quoteInfo)
    {
        //withing book quote the system prompts the user to pay
        Invoice possibleInvoice = bikeSystem.bookQuote(q,quoteInfo);
        if(possibleInvoice != null) //if the invoice is null, we cant book the quote, system state has changed 
        {
            currentInvoices.add(possibleInvoice);
            return true;
        }
        else
        {
            //we couldnt order the quote
            return false
            //in the normal system I/O would happen here
        }
    }

    // tells the system if the user needs a new selection of quotes,
    //in the actual system the boolean parameter wouldnt be required
    public boolean evaluateQuotes(List<Quote> quotesGiven,boolean satisfied)
    {
        //in our actual system, this would be where i/o happens, but
        //we were told not to care about that, so yeah
        if(satisfied)
        {
            return true;
        }
        else
        {
            return false;
        }
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


