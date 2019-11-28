package uk.ac.ed.bikerental;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import uk.ac.ed.bikerental.Utils.EBikeType;
import uk.ac.ed.bikerental.Utils.EBookingStatus;
import uk.ac.ed.bikerental.Utils.ECollectionMode;
import uk.ac.ed.bikerental.Utils.ECondition;
import java.time.LocalDate;

public class TestBikeProvider
{
    private BikeProvider bpr;
    private DateRange dr1,dr2,dr3;
    private Bike bike1,bike2;
    private Collection<EBikeType> bikeOrder1, bikeOrder2,bikeOrder3;
    private BikeRentalSystem brs;
    private PricingPolicy pPol;
    @BeforeEach
    void setUp()
    {
        //to be booked
        dr1 = new DateRange(LocalDate.of(2019,1,1), LocalDate.of(2019,1,7));
        //after booked
        dr2 = new DateRange(LocalDate.of(2019,1,8), LocalDate.of(2019,1,14));
        //overlaps booked 
        dr3 = new DateRange(LocalDate.of(2019,1,6), LocalDate.of(2019,1,9));

        DeliveryServiceFactory.setupMockDeliveryService();
        brs = new BikeRentalSystem(LocalDate.of(2019,1,1));

        float drate = 0.5f;
        ValuationPolicy vPol = new StandardValuationPolicy(drate);
        pPol = new StandardPricingPolicy();

        BikeType bt1 = new BikeType(EBikeType.MOUNTAIN,new BigDecimal(100)); 
        pPol.setDailyRentalPrice(bt1,new BigDecimal(10));

        BikeType bt2 = new BikeType(EBikeType.HYBRID, new BigDecimal(100));
        pPol.setDailyRentalPrice(bt2, new BigDecimal(10));
        //1 bike
        bikeOrder1 = new ArrayList<EBikeType>();
        bikeOrder1.add(EBikeType.MOUNTAIN);

        //2 bikes
        bikeOrder2 = new ArrayList<EBikeType>();
        bikeOrder2.add(EBikeType.MOUNTAIN);
        bikeOrder2.add(EBikeType.MOUNTAIN);

        //2 bikes mixed
        bikeOrder3 = new ArrayList<EBikeType>();
        bikeOrder3.add(EBikeType.MOUNTAIN);
        bikeOrder3.add(EBikeType.HYBRID);

        bike1 = new Bike(bt1,LocalDate.of(2016,2,5),ECondition.NEW);
        bike2 = new Bike(bt2,LocalDate.of(2016,2,5),ECondition.NEW);
        bpr = new BikeProvider(brs,new Location("EH11 8SY","69 Street"),vPol,pPol);
        bpr.addBike(bike1);
        bpr.addBike(bike2);
    }

    @Test
    void testIDs()
    {
        //reset the static id counter
        int lastTakenID = BikeProvider.getIDCounter();
        BikeRentalSystem brs = null;
        BikeProvider bp1 = new BikeProvider(brs,new Location("asdasds","asdasd"),new StandardValuationPolicy(1f), new StandardPricingPolicy());
        assertEquals(lastTakenID+1,bp1.getId(),"first provider was set ID: " + bp1.getId());

        BikeProvider bp2 = new BikeProvider(brs,new Location("asdasds","asdasd"),new StandardValuationPolicy(1f), new StandardPricingPolicy());
        assertEquals(lastTakenID+2,bp2.getId(),"second provider was set ID: "+ bp2.getId());
    }

    @Test
    void testCanAccomodate()
    {
        //set up
        Quote outQuote = bpr.createQuote(dr1, bikeOrder1);
        //test
        
        //only bike we could possibly receive
        assert(outQuote.getBikes().contains(bike1));
        //we asked for one bike
        assertEquals(1,outQuote.getBikes().size());
        //the quote should cover the date range we wanted

        //TODO possibly move this to system tests, in general make sure the tests are 'UNIT tests', idk man, it's 6 am and my bed is looking more attractive than a jar of pickles
        assertEquals(dr1,outQuote.getDates());
        assertEquals(new BigDecimal(70).stripTrailingZeros(),outQuote.getPrice().stripTrailingZeros());
        assertEquals(new BigDecimal(50).stripTrailingZeros(),outQuote.getDeposit().stripTrailingZeros());
    }

    @Test
    void testCantAccomodate()
    {
        //set up
        Quote outQuote = bpr.createQuote(dr1, bikeOrder1);
        QuoteInformation qInfo = new QuoteInformation();
        bpr.createBooking(outQuote, qInfo);
        //test

        //dates overlap fully
        assertEquals(false, bpr.canAccommodateRental(dr1, bikeOrder1));
        //not enough bikes
        assertEquals(false, bpr.canAccommodateRental(dr2, bikeOrder2));
        //dates overlap partially
        assertEquals(false, bpr.canAccommodateRental(dr3, bikeOrder1));
    }

    @Test
    void testCreateBooking()
    {
        //setup
        Quote outQuote = bpr.createQuote(dr1, bikeOrder1);
        QuoteInformation qInfo = new QuoteInformation();
        qInfo.collectionMode = ECollectionMode.DELIVERY;
        qInfo.address = new Location("EH12 59T","canine lovers 4/1");
        //test
        Booking bookingOut = bpr.createBooking(outQuote, qInfo);

        //see if the booking was added
        assertEquals(true,bpr.containsBooking(bookingOut));

        //check the bookings fields are what we ordered in the quote

        //same amount of bikes
        assertEquals(outQuote.getBikes().size(),bookingOut.getBikeCodes().size());
        //same bikes
        int totalBikes = outQuote.getBikes().size();
        for(Bike b : outQuote.getBikes())
        {
            for(int code : bookingOut.getBikeCodes())
            {
                if(b.getCode() == code)
                {
                    totalBikes--;
                }
            }
        }
        assertEquals(0,totalBikes);
        //same dates
        assertEquals(outQuote.getDates(),bookingOut.getDates());
        //same price and deposit
        assertEquals(outQuote.getPrice(), bookingOut.getPrice());
        assertEquals(outQuote.getDeposit(), bookingOut.getDeposit());
        //same provider info
        assertEquals(outQuote.getProvider().getId(), bookingOut.getProviderID());
        //same quote information

        //now check the booking has been added to the bikes appropriately
        assert(bike1.containsBooking(bookingOut));
    }

    @Test
    void testUpdateBooking()
    {
        //setup
        Quote outQuote = bpr.createQuote(dr1, bikeOrder1);
        QuoteInformation qInfo = new QuoteInformation();
        qInfo.collectionMode = ECollectionMode.DELIVERY;
        qInfo.address = new Location("EH12 59T","canine lovers 4/1");
        Booking bookingOut = bpr.createBooking(outQuote, qInfo);
        //test
        
        //check the update is succesfull
        try {
            bpr.updateBooking(bookingOut.getOrderCode(),EBookingStatus.DELIVERY_TO_CLIENT);
        } catch (Exception e) {
            assert(false);
        }
        //check the booking updates correctly
        assertEquals(EBookingStatus.DELIVERY_TO_CLIENT,bookingOut.getStatus());

        //now check if bookings are removed from appropriate bikes on RETURNED
        try {
            bpr.updateBooking(bookingOut.getOrderCode(), EBookingStatus.RETURNED);
        } catch (Exception e){
            //check we could update
            assert(false);
        }
        //first update was correct
        assertEquals(EBookingStatus.RETURNED,bookingOut.getStatus());
        //then see if it was removed
        assertEquals(false,bike1.containsBooking(bookingOut));    
    }

    @Test
    void testMixedTypeBikesQuote()
    {
        //let's add some other bikes to the provider
        //other bike type
        BikeType btfiller = new BikeType(EBikeType.ROAD,new BigDecimal(100));
        Bike bikefiller1 = new Bike(btfiller,LocalDate.of(2019,1,1),ECondition.AVERAGE);
        Bike bikefiller2 = new Bike(btfiller,LocalDate.of(2019,1,1),ECondition.AVERAGE);
        Bike bikefiller3 = new Bike(btfiller,LocalDate.of(2019,1,1),ECondition.AVERAGE);

        //let's create a new bike of a different type

        BikeType btWanted = new BikeType(EBikeType.ELECTRIC, new BigDecimal(100));
        Bike bikeWanted = new Bike(btWanted, LocalDate.of(2019,1,1),ECondition.AVERAGE);

        //let's create an order for an electric bike and a mountain bike

        List<EBikeType> order = new ArrayList<EBikeType>();
        order.add(EBikeType.ELECTRIC);
        order.add(EBikeType.MOUNTAIN);
        pPol.setDailyRentalPrice(btfiller, new BigDecimal(5));
        pPol.setDailyRentalPrice(btWanted, new BigDecimal(5));
        

        bpr.addBike(bikefiller1);
        bpr.addBike(bikefiller2);
        bpr.addBike(bikefiller3);
        bpr.addBike(bikeWanted);

        Quote outQuote = bpr.createQuote(dr1, order);
        //we are expecting
        boolean b1Returned = false;
        boolean bWantedReturned = false;

        for(Bike b : outQuote.getBikes())
        {
            if(b == bike1)
            {
                b1Returned = true;
            }
            if(b == bikeWanted)
            {
                bWantedReturned = true;
            }
        }
        assertTrue(b1Returned,"mountain bike wasn't returned");
        assertTrue(bWantedReturned,"electric bike wasn't returned");

    }
}