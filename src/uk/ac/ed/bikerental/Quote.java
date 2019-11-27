package uk.ac.ed.bikerental;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Quote {
	private BikeProvider provider;
	private BigDecimal price;
	private BigDecimal deposit;
	private ArrayList<Bike> bikes;
	private DateRange dates;
	
	public Quote(BikeProvider pProvider, BigDecimal pPrice, BigDecimal pDeposit, List<Bike> pBikes, DateRange pDates) {
		provider = pProvider;
		price = pPrice;
		deposit = pDeposit;
		bikes = new ArrayList<Bike>(pBikes);
		dates = pDates;
	}
	
	public BigDecimal getPrice() { return price; }
	public BigDecimal getDeposit() {return deposit; }
	public ArrayList<Bike> getBikes() {return bikes; }
	public DateRange getDates() {return dates; }
	public BikeProvider getProvider() {return provider;}
}
