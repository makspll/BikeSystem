package uk.ac.ed.bikerental;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

import uk.ac.ed.bikerental.Utils.EBikeType;

public class BikeRentalSystem {

	private LinkedList<BikeProvider> bikeProviders;
	private LinkedList<BikeType> bikeTypes;
	
	public LinkedList<Quote> getQuotes(DateRange dates, Collection<EBikeType> bikes, Location loc) throws Exception  {
		
		if (bikes.isEmpty()) throw new Exception("You cannot request quotes for an empty collection of bikes.");
		
		LinkedList<Quote> quotes = new LinkedList<Quote>();
		
		for (BikeProvider prov : bikeProviders) {
			
			if (prov.getLocation().isNearTo(loc)) {
				if (prov.canAccomodateRental(dates, bikes)) {
					Quote newQuote = new Quote(prov , );
				}
			}
			
		}
		
		return quotes;
	}
	
}
