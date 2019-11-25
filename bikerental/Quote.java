package uk.ac.ed.bikerental;

import java.math.BigDecimal;
import java.util.LinkedList;

public class Quote {
	private BikeProvider provider;
	private BigDecimal price;
	private BigDecimal deposit;
	private LinkedList<Bike> bikes;
	private DateRange dates;
	
	public Quote(BikeProvider pProvider, BigDecimal pPrice, BigDecimal pDeposit, DateRange pDates) {
		provider = pProvider;
		price = pPrice;
		deposit = pDeposit;
		bikes = new LinkedList<Bike>();
		dates = pDates;
	}
	
	public BigDecimal getPrice() { return price; }
	public BigDecimal getDeposit() {return deposit; }
	public LinkedList<Bike> getBikes() {return bikes; }
	public DateRange getDates() {return dates; }
}
