package uk.ac.ed.bikerental;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedList;

import uk.ac.ed.bikerental.Utils.BookingStatusEnum;

public class Booking {

	private String orderCode;
	private BigDecimal deposit;
	private BigDecimal price;
	private BookingStatusEnum status;
	private LinkedList<String> bikeCodes;
	private LocalDate expiryDate;
	private BikeProvider providerID;
	
	public Booking(String pCode, BigDecimal pDeposit, BigDecimal pPrice , LinkedList<String> pBikeCodes , LocalDate pExpiryDate, BikeProvider pProviderID) {
		orderCode = pCode;
		deposit = pDeposit;
		price = pPrice;
		status = BookingStatusEnum.BOOKED;
		bikeCodes = pBikeCodes;
		expiryDate = pExpiryDate;
		providerID = pProviderID;
	}	
	
	public void setBookingStatus(BookingStatusEnum newStatus) {
		status = newStatus;
	}
	
	public void progressBooking(boolean orderly) {
		// I am not 100% sure what this does, this is just a first skeleton and probably not useful:
		switch (status) {
			case BOOKED: 
				status = orderly ? BookingStatusEnum.PICKEDUP : BookingStatusEnum.BIKESONWAYBACK;
				break;
			case PICKEDUP:
				status = orderly ? BookingStatusEnum.RETURNED : BookingStatusEnum.BIKESONWAYBACK;
				break;
			case BIKESONWAYBACK:
				status = BookingStatusEnum.RETURNED;
				break;
		}
	}
	
}
