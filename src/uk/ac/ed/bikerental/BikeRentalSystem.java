package uk.ac.ed.bikerental;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import uk.ac.ed.bikerental.Utils.EBikeType;
import uk.ac.ed.bikerental.Utils.EBookingStatus;
import uk.ac.ed.bikerental.Utils.ECollectionMode;
import uk.ac.ed.bikerental.Utils.ECondition;

public class BikeRentalSystem {

	private List<BikeProvider> bikeProviders;
	private List<BikeType> bikeTypes;
	private LocalDate currentDate;
	private PaymentHandler paymentHandler;

	public List<BikeProvider> getProviders() { return bikeProviders; }
	public List<EBikeType> getEBikeTypes() {
		List<EBikeType> eTypes = new ArrayList<EBikeType>();
		for(BikeType b : bikeTypes)
		{
			eTypes.add(b.getType());
		}
		return eTypes; 
		}
	
	public BikeRentalSystem(LocalDate dateInitial)
	{
		bikeProviders = new ArrayList<BikeProvider>();
		bikeTypes = new ArrayList<BikeType>();
		currentDate = dateInitial;
		paymentHandler = new PaymentHandler();
	}
	
	public BikeType registerBikeType(EBikeType Ebt, BigDecimal replacementVal) throws Exception
	{
		for(BikeType bt : bikeTypes)
		{
			if(bt.getType() == Ebt)
			{
				throw new Exception("this bike type has already been registered");
			}
		}

		BikeType newBt = new BikeType(Ebt, replacementVal);
		bikeTypes.add(newBt);
		return newBt;
	}
	
	public BikeType getType(EBikeType type) throws Exception {
		for (BikeType bt : bikeTypes) {
			if (bt.getType() == type) return bt;
		}
		throw new Exception("Type has not been registered yet!");
	}

	public int registerBike(BikeType bt, ECondition cond, LocalDate madeOn, int providerID) throws Exception
	{
		if (!bikeTypes.contains(bt)) {
			throw new Exception("Cannot add a bike of an unregistered type.");
		}
		boolean containsID = false;
		for (BikeProvider bp : this.bikeProviders) {
			if (bp.getId() == providerID) containsID = true;
		}
		if (!containsID) throw new Exception("The provider ID is not in the system");
		
		Bike newBike = new Bike(bt,madeOn,cond);
		getProviderWithID(providerID).addBike(newBike);
		return newBike.getCode();
	}
	
	public BikeProvider getProviderWithID(int id) throws Exception {
		for (BikeProvider bp : bikeProviders) {
			if (bp.getId() == id) return bp;
		}
		throw new Exception("Provider with this ID has not been found");
	}

	public int registerProvider(Location loc, ValuationPolicy vp, PricingPolicy pp)
	{
		BikeProvider bp = new BikeProvider(this,loc,vp,pp);
		bikeProviders.add(bp);
		return bp.getId();
	}
	
	public LinkedList<Quote> getQuotes(DateRange dates, Collection<EBikeType> bikes, Location loc) throws Exception  {
		
		if (bikes.isEmpty()){throw new Exception("You cannot request quotes for an empty collection of bikes.");}
		
		LinkedList<Quote> quotes = new LinkedList<Quote>();
		
		//when finding a quote, we can only include providers near the selected location,
		//and out of those, those who have the capacity to accommodate the choice of bikes

		for (BikeProvider prov : bikeProviders) {
			if (prov.getLocation().isNearTo(loc)) {
				if (prov.canAccommodateRental(dates, bikes)) {
					Quote newQuote = prov.createQuote(dates, bikes);
					quotes.add(newQuote);
				}
			}
		
		}
		
		return quotes;
	}
	
	public Invoice bookQuote(Quote q, QuoteInformation quoteInfo) throws Exception
	{

		BikeProvider responsibleProvider = q.getProvider();

		//check if we can still accommodate the quote, and if the state has changed since acquiring the quote (e.g. the prices)
		ArrayList<EBikeType> bikeTypes = new ArrayList<EBikeType>();
		Quote otherQuote;
		for(Bike b : q.getBikes())
		{
			bikeTypes.add(b.getBikeType().getType());
		}

		if(responsibleProvider.canAccommodateRental(q.getDates(),bikeTypes))
		{	
			// Here we create a quote with the same prerequisites as the one that was originally supposed to be ordered. 
			// We do this to account for the possibility that, even if one or more bikes have been reserved since
			// the original quote was created, the customer may be able to receive a quote with bikes of the same types, for the same price/deposit. 
			otherQuote = responsibleProvider.createQuote(q.getDates(), bikeTypes);
			
			if(areQuotesEquivalent(q, otherQuote) == false)
			{
				throw new Exception("State has changed, quote cannot be accommodated at the same price anymore.");
			}
		} else throw new Exception("State has changed, quote cannot be accommodated anymore.");
		

		Booking createdBooking = responsibleProvider.createBooking(otherQuote, quoteInfo);
		Location pickup = (createdBooking.getCollectionMode() == ECollectionMode.DELIVERY)? quoteInfo.address:responsibleProvider.getLocation();
		String summary = "OrderNo: " + createdBooking.getOrderCode() + '\n'+
						 "Pickup: " + pickup.toString() + '\n' +
						 "Return To: " + responsibleProvider.toString() + '\n' +
						 "Return By: " + otherQuote.getDates().getEnd().toString() + '\n';

		Invoice invoice = new Invoice(createdBooking,
									  summary, 
									  responsibleProvider.getLocation(),
									  pickup,
									  quoteInfo.
									  collectionMode);

		//schedule delivery for each bike, if we are dealing with a delivery 
		if (quoteInfo.collectionMode == ECollectionMode.DELIVERY) {
			for(Bike b : otherQuote.getBikes())
			{
				b.addBooking(createdBooking);
				DeliveryService deliveryService = DeliveryServiceFactory.getDeliveryService();
				deliveryService.scheduleDelivery(b, responsibleProvider.getLocation(), quoteInfo.address, q.getDates().getStart());
			}
		}
		
		
		paymentHandler.payTo("This function call is merely a placeholder, since we don't need to worry about payments");

		return invoice;
	}
	
	private boolean areQuotesEquivalent(Quote q1, Quote q2) {
		return q1.getPrice().stripTrailingZeros().equals(q2.getPrice().stripTrailingZeros())
				&& q2.getDeposit().stripTrailingZeros().equals(q2.getDeposit().stripTrailingZeros());
	}

	public void recordReturnToOriginalProvider(int bookingNo) throws Exception
	{
		for(BikeProvider bp : bikeProviders)
		{
			if(bp.containsBooking(bookingNo))
			{
				try {
					bp.updateBooking(bookingNo, EBookingStatus.RETURNED);
					return;
				} catch (Exception e) {
					//we checked the provider contains the booking
					assert(false);
				}
			}
		}
		throw new Exception("provider with such booking could not be found");
	}

	public void recordBikeReturnToPartnerProvider(int bookingNo, int partnerId) throws Exception
	{
		BikeProvider partner = null;
		for(BikeProvider bp: bikeProviders)
		{
			if(bp.getId() == partnerId)
			{
				partner = bp;
				break;
			}
		}
		if(partner == null) throw new Exception("couldn't find such partner");
	

		for(BikeProvider bp : bikeProviders)
		{
			if(bp.containsBooking(bookingNo))
			{
				try {
					//update booking status
					Booking booking = bp.getBooking(bookingNo);
					List<Bike> bikes = new ArrayList<Bike>();
					for(Integer code : booking.getBikeCodes())
					{
						bikes.add(bp.getBikeWithCode(code));
					}
					//schedule deliveries
					assert(bikes.size() > 0);
					for(Bike b : bikes)
					{
	
						DeliveryService deliveryService = DeliveryServiceFactory.getDeliveryService();
						deliveryService.scheduleDelivery(b,partner.getLocation(), bp.getLocation(), currentDate);
						assert(((MockDeliveryService)deliveryService).pickups.containsKey(currentDate));
					}
				} catch (Exception e) {
					//we checked the provider contains the booking earlier
					throw new Exception("code shouldn't be reached");
				}
			}
		}
	}

	public Booking findBooking(int bookingNo) throws Exception
	{
		for(BikeProvider b : bikeProviders)
		{
			if(b.containsBooking(bookingNo))
			{
				try{
					return(b.getBooking(bookingNo));
				}catch(Exception e)
				{
					throw new Exception("cannot find booking");
				}
			}
		}
		return null;
	}
	public void stepDateForward()
	{
		currentDate = currentDate.plusDays(1);
	}

	public void setDate(LocalDate newDate)
	{
		currentDate = newDate;
	}

	public LocalDate getDate()
	{
		return currentDate;
	}
}