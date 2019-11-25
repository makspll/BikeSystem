package uk.ac.ed.bikerental;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.LinkedList;

import uk.ac.ed.bikerental.Utils.EBikeType;

public class Quote {
	private BikeProvider provider;
	private BigDecimal price;
	private BigDecimal deposit;
	private LinkedList<Bike> bikes;
	private DateRange dates;
	
	public Quote(BikeProvider pProvider, BigDecimal pPrice, BigDecimal pDeposit, Collection<Bike> pBikes, DateRange pDates) {
		provider = pProvider;
		price = pPrice;
		deposit = pDeposit;
		bikes = (LinkedList<Bike>) pBikes;
		dates = pDates;
	}
	
	public BigDecimal getPrice() { return price; }
	public BigDecimal getDeposit() {return deposit; }
	public LinkedList<Bike> getBikes() {return bikes; }
	public DateRange getDates() {return dates; }
}
