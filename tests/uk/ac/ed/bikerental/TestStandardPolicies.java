package uk.ac.ed.bikerental;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.*;

import uk.ac.ed.bikerental.Utils.*;

public class TestStandardPolicies
{
    private PricingPolicy pp;
    private ValuationPolicy vp;
    private BikeProvider bpr;
    private Bike bHybrid,bMountain;
    private List<Bike> bikeorder1,bikeorder2,bikeorder3;
    private DateRange dr;
    @BeforeEach
    void setUp()
    {
        BikeRentalSystem brs = new BikeRentalSystem(LocalDate.now());
        BikeType bt1 = new BikeType(EBikeType.HYBRID,new BigDecimal(100));  //deposit    - hybrid    => 100 
        BikeType bt2 = new BikeType(EBikeType.MOUNTAIN, new BigDecimal(50));//deposit    - mountain  => 50
        pp = new StandardPricingPolicy(); 
        pp.setDailyRentalPrice(bt1, new BigDecimal(10));                    //daily price - hybrid   => 10
        pp.setDailyRentalPrice(bt2, new BigDecimal(15));                    //daily price - mountain => 15

        vp = new StandardValuationPolicy(0.5f); //deposit rate = 0.5f
        bpr = new BikeProvider(brs,new Location("asdadsd","asdasd"),vp,pp);

        //have 3 bikes to calculate values on
        bHybrid = new Bike(bt1,LocalDate.now(),ECondition.AVERAGE);
        bMountain = new Bike(bt2,LocalDate.now(),ECondition.AVERAGE);

        //1 day orders
        dr = new DateRange(LocalDate.of(2019, 1, 1), LocalDate.of(2019,1,1));
        //set up orders
        bikeorder1 = new ArrayList<Bike>();
        bikeorder1.add(bHybrid);
        bikeorder1.add(bHybrid);   //total deposit = (100 * 0.5 * 2) + 50 * 0.5 = 125
        bikeorder1.add(bMountain);  //total price = 35 

        bikeorder2 = new ArrayList<Bike>();
        bikeorder2.add(bMountain);
        bikeorder2.add(bHybrid);   //total deposit =(50 * 0.5 * 2) + 100 * 0.5 = 100
        bikeorder2.add(bMountain); //total price = 40


        bikeorder3 = new ArrayList<Bike>();
        bikeorder3.add(bMountain);
        bikeorder3.add(bMountain); //total deposit = 50 * 0.5 * 3 = 75
        bikeorder3.add(bMountain); //total price = 45

    }

    @Test
    void testStandardPricingPolicy()
    {
        //test the default pricing implementation
        //we already set up the daily prices for each type in our setup, so if we reached this test the set up was correct
        BigDecimal bikeOrder1Price = new BigDecimal(35.00).setScale(2,RoundingMode.HALF_UP).stripTrailingZeros();
        BigDecimal bikeOrder2Price = new BigDecimal(40.00).setScale(2,RoundingMode.HALF_UP).stripTrailingZeros();
        BigDecimal bikeOrder3Price = new BigDecimal(45.00).setScale(2,RoundingMode.HALF_UP).stripTrailingZeros();

        //check the prices are correct
        assertEquals(bikeOrder1Price,pp.calculatePrice(bikeorder1, dr).stripTrailingZeros());
        assertEquals(bikeOrder2Price,pp.calculatePrice(bikeorder2, dr).stripTrailingZeros());
        assertEquals(bikeOrder3Price,pp.calculatePrice(bikeorder3, dr).stripTrailingZeros());
        
    }


    @Test
    void testStandardValuationPolicy()
    {
        //test the default valuation implementation
        //we already set up the policy  so if we reached this test the set up was correct
        BigDecimal bikeOrder1Price = new BigDecimal(125.00).setScale(2,RoundingMode.HALF_UP).stripTrailingZeros();
        BigDecimal bikeOrder2Price = new BigDecimal(100.00).setScale(2,RoundingMode.HALF_UP).stripTrailingZeros();
        BigDecimal bikeOrder3Price = new BigDecimal(75.00).setScale(2,RoundingMode.HALF_UP).stripTrailingZeros();

        //deposit totals
        BigDecimal deposit1 = vp.calculateValue(bikeorder1.get(0), LocalDate.now()).add(
                            vp.calculateValue(bikeorder1.get(1), LocalDate.now()).add( 
                            vp.calculateValue(bikeorder1.get(2), LocalDate.now())));

        BigDecimal deposit2 = vp.calculateValue(bikeorder2.get(0), LocalDate.now()).add(
                            vp.calculateValue(bikeorder2.get(1), LocalDate.now()).add( 
                            vp.calculateValue(bikeorder2.get(2), LocalDate.now())));

        BigDecimal deposit3 = vp.calculateValue(bikeorder3.get(0), LocalDate.now()).add(
                            vp.calculateValue(bikeorder3.get(1), LocalDate.now()).add( 
                            vp.calculateValue(bikeorder3.get(2), LocalDate.now())));
        //check the deposits are correct
        assertEquals(bikeOrder1Price,deposit1.stripTrailingZeros());
        assertEquals(bikeOrder2Price,deposit2.stripTrailingZeros());
        assertEquals(bikeOrder3Price,deposit3.stripTrailingZeros());

    }
}