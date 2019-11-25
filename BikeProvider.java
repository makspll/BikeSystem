package uk.ac.ed.bikerental;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

import uk.ac.ed.bikerental.Utils.EBikeType;

public class BikeProvider {

	private LinkedList<Booking> allBookings;
	private int providerID;
	private Location location;
	private LinkedList<Bike> bikes;
	private LinkedList<BikeProvider> partners;
	private float depositRate;
	private String phoneNumber;
	private String openingTimes;
	
	public boolean canAccomodateRental(DateRange dr, Queue<EBikeType> expectedBikeTypes) {
		
		HashMap<EBikeType, Integer> remainingNumOfBikesPerType = new HashMap<EBikeType, Integer>();
		
		for (EBikeType type : EBikeType.values()) {
			remainingNumOfBikesPerType.put(type, 0);
		}
		
		////WRONG! WE NEED TO CHECK IF THE BIKE IS AVAILABLE IN THE DATE RANGE! HOW? ATTACH COLLECTION OF BOOKINGS TO BIKE OR SMTH?
		for (Bike b : bikes) {
			EBikeType type = b.getBikeType().getType();
			remainingNumOfBikesPerType.put(type, remainingNumOfBikesPerType.get(type) + 1);
		}
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		for (EBikeType type : expectedBikeTypes) {
			remainingNumOfBikesPerType.put(type, remainingNumOfBikesPerType.get(type) - 1);
		}
		
		return false;
		
	}
	
	// TODO: REMAINING METHODS
}
