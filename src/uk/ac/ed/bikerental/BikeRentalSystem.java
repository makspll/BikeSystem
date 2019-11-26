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
	private DeliveryService deliveryService;
	private LocalDate currentDate;

	public List<BikeProvider> getProviders() { return bikeProviders; }
	public List<BikeType> getBikeTypes() { return bikeTypes; }
	
	static int UNIQUE_ID_COUNTER = 0;
	public BikeRentalSystem(DeliveryService ds, LocalDate dateInitial)
	{
		bikeProviders = new ArrayList<BikeProvider>();
		bikeTypes = new ArrayList<BikeType>();
		deliveryService = ds;
		currentDate = dateInitial;
	}
	
	public void registerBikeType(EBikeType Ebt, BigDecimal replacementVal) throws Exception
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
	}

	public Bike registerBike(BikeType bt, ECondition cond, LocalDate madeOn) throws Exception
	{
		if (!bikeTypes.contains(bt)) {
			throw new Exception("Cannot add a bike of an unregistered type.");
		}
		
		Bike newBike = new Bike(bt,madeOn,cond);
		return newBike;
	}

	public void registerProvider(Location loc, String phone, String openingTimes, ValuationPolicy vp, PricingPolicy pp)
	{
		BikeProvider bp = new BikeProvider(++UNIQUE_ID_COUNTER,loc,phone,openingTimes,vp,pp);
		bikeProviders.add(bp);
	}
	
	public LinkedList<Quote> getQuotes(DateRange dates, Collection<EBikeType> bikes, Location loc) throws Exception  {
		
		if (bikes.isEmpty()) throw new Exception("You cannot request quotes for an empty collection of bikes.");
		
		LinkedList<Quote> quotes = new LinkedList<Quote>();
		
		//when finding a quote, we can only include providers near the selected location,
		//and out of those, those who have the capacity to accomodate the choice of bikes
		for (BikeProvider prov : bikeProviders) {
			if (prov.getLocation().isNearTo(loc)) {
				if (prov.canAccomodateRental(dates, bikes)) {
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

		//check we can still accomodate the quote, and if the state has changed since acquiring the quote (e.g. the prices)
		ArrayList<EBikeType> bikeTypes = new ArrayList<EBikeType>();
		Quote otherQuote;
		for(Bike b : q.getBikes())
		{
			bikeTypes.add(b.getBikeType().getType());
		}

		if(responsibleProvider.canAccomodateRental(q.getDates(),bikeTypes))
		{
			otherQuote = responsibleProvider.createQuote(q.getDates(), bikeTypes);
			if(q.getPrice() != otherQuote.getPrice() || q.getDeposit() != otherQuote.getDeposit())
			{
				throw new Exception("State has changed, quote cannot be accomodated anymore, or at the same price.");
			}
		}
		

		Booking createdBooking = responsibleProvider.createBooking(q, quoteInfo);
		Location pickup = (createdBooking.getCollectionMode() == ECollectionMode.DELIVERY)? quoteInfo.address:responsibleProvider.getLocation();
		String summary = "OrderNo: " + createdBooking.getOrderCode() + '\n'+
						 "Pickup: " + pickup.toString() + '\n' +
						 "Return To: " + responsibleProvider.toString() + '\n' +
						 "Return By: " + q.getDates().getEnd().toString() + '\n';

		Invoice invoice = new Invoice(createdBooking.getOrderCode(),
									  summary, 
									  createdBooking.getDeposit(), 
									  createdBooking.getPrice(),
									  createdBooking.getBikeCodes(),
									  responsibleProvider.getLocation(),
									  pickup,
									  quoteInfo.
									  collectionMode,
									  q.getDates().getEnd());

		//schedule delivery for each bike
		for(Bike b : q.getBikes())
		{
			deliveryService.scheduleDelivery(b, responsibleProvider.getLocation(), quoteInfo.address, q.getDates().getStart());
		}

		return invoice;
	}

	public void recordReturnToOriginalProvider(int bookingNo)
	{
		for(BikeProvider bp : bikeProviders)
		{
			if(bp.containsBooking(bookingNo))
			{
				try {
					bp.updateBooking(bookingNo, EBookingStatus.RETURNED);
				} catch (Exception e) {
					//we checked the provider contains the booking
					assert(false);
				}
			}
		}
	}

	public void recordBikeReturnToPartnerProvider(int bookingNo, int partnerId)
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
		assert(partner != null);

		for(BikeProvider bp : bikeProviders)
		{
			if(bp.containsBooking(bookingNo))
			{
				try {
					//update booking status
					bp.updateBooking(bookingNo, EBookingStatus.DELIVERY_TO_PROVIDER);
					List<Bike> bikes = bp.getBikesFromBooking(bookingNo);
					//schedule deliveries
					for(Bike b : bikes)
					{
						deliveryService.scheduleDelivery(b,partner.getLocation(), bp.getLocation(), currentDate);
					}
				} catch (Exception e) {
					//we checked the provider contains the booking earlier
					assert(false);
				}
			}
		}
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
