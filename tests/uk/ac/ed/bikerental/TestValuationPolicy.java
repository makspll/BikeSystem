package uk.ac.ed.bikerental;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;

import uk.ac.ed.bikerental.ValuationPolicy;
import uk.ac.ed.bikerental.Utils.EBikeType;
import uk.ac.ed.bikerental.Utils.ECondition;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;



//this is the 'other extension' policy
public class TestValuationPolicy
{

    //mock testing class, we don't care about what value it actually calculates, we are testing if it interfaces with our design correctly
    public class MockValuationExtension implements ValuationPolicy
    {
        @Override
        public BigDecimal calculateValue(Bike bike, LocalDate date) {
            //access some properties of the bike
            Utils.ECondition c = bike.getCondition();
            BikeType bt = bike.getBikeType();
            boolean available = bike.inStore();
            //access the date
            int day = date.getDayOfMonth();

            //return a value
            return new BigDecimal(1);
        }
    }
    private BikeRentalSystem brs;
    private BikeProvider bpr;
    private Bike b1;
    private ValuationPolicy vp;
    private PricingPolicy pp;

    @BeforeEach
    void setUp()
    {
        //set up a generic system
        brs = new BikeRentalSystem(LocalDate.now());
        //instantiate a mock valuation policy
        vp = new MockValuationExtension();
        //set up default pricing policy
        pp = new StandardPricingPolicy();
        //add a bike type
        BikeType bt = new BikeType(EBikeType.ROAD,new BigDecimal(0));
        pp.setDailyRentalPrice(bt, new BigDecimal(0));
        //some bike provider
        bpr = new BikeProvider(brs,new Location("abcdefg","asdasddsa"),vp,pp);
        //some bike of the type we just created
        b1 = new Bike(bt,LocalDate.now(),ECondition.GOOD);

        //lets register the bike
        bpr.addBike(b1);
    }

    @Test
    void testInterfaceWithOtherClasses()
    {
        //check the valuation policy is set up correctly and if we can use it
        try{
            assertTrue(bpr.getValuationPolicy().calculateValue(b1, LocalDate.now()) != null, "valuation policy did not give a valid value");
        }catch (Exception e)
        {
            assertTrue(false,"some exception is thrown by the valuation policy");
        }

        //the valuation policy is used within create quote, let's see if there are any problems
        DateRange dr = new DateRange(LocalDate.now(),LocalDate.now());
        List<EBikeType> order = new ArrayList<EBikeType>();
        order.add(EBikeType.ROAD);
        
        //order for a single road bike
        Quote q = bpr.createQuote(dr, order);

        //we should receive one quote
        assertTrue(q != null,"did not receive quotes");
        //check the quote has some deposit value
        assertTrue(q.getDeposit() != null,"deposit value is wrong");

    }
}