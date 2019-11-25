package uk.ac.ed.bikerental;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedList;


import uk.ac.ed.bikerental.Utils.EBookingStatus;

public class Booking {

	private int orderCode;
	private BigDecimal deposit;
	private BigDecimal price;
	private EBookingStatus status;
	private LinkedList<Integer> bikeCodes;
	private LocalDate expiryDate;
	private BikeProvider providerID;
	
	private static int UNIQUE_CODE_COUNT = 0;
	
	public Booking(BigDecimal pDeposit, BigDecimal pPrice , LinkedList<Integer> pBikeCodes , LocalDate pExpiryDate, BikeProvider pProviderID) {
		orderCode = ++UNIQUE_CODE_COUNT;									// This might work for now. 
		deposit = pDeposit;
		price = pPrice;
		status = EBookingStatus.BOOKED;
		bikeCodes = pBikeCodes;
		expiryDate = pExpiryDate;
		providerID = pProviderID;
	}	
	
	public void setBookingStatus(EBookingStatus newStatus) {
		status = newStatus;
	}
	
	public void progressBooking(boolean orderly) {
		// I am not 100% sure what this does, this is just a first skeleton and probably not useful:
		switch (status) {
			case BOOKED: 
				status = orderly ? EBookingStatus.PICKED_UP : EBookingStatus.BIKES_ON_WAY_BACK;
				break;
			case PICKED_UP:
				status = orderly ? EBookingStatus.RETURNED : EBookingStatus.BIKES_ON_WAY_BACK;
				break;
			case BIKES_ON_WAY_BACK:
				status = EBookingStatus.RETURNED;
				break;
		}
	}
	
	public int getOrderCode() { return orderCode; }
	public EBookingStatus getStatus() { return status; }
	
}
