package uk.ac.ed.bikerental;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Arrays;
public class TestMultiDayRateDiscountPolicy {
    // You can add attributes here
    MultidayRateDiscountPolicy policy1;
    Bike b1,b2,b3;
    List<Bike> bikes;
    DateRange r1,r2,r3,r6,r7,r13,r14,r50;

    @BeforeEach
    void setUp() throws Exception {
        // Put setup here
        BikeType mountain = new BikeType(Utils.EBikeType.MOUNTAIN,
        new BigDecimal("200"));
        BikeType road = new BikeType(Utils.EBikeType.ROAD,
        new BigDecimal("150"));
        BikeType electric = new BikeType(Utils.EBikeType.ELECTRIC,
        new BigDecimal("300"));

        policy1 = new MultidayRateDiscountPolicy(2, 6, 13, 5);
        policy1.setDailyRentalPrice(mountain, new BigDecimal(10).stripTrailingZeros());
        policy1.setDailyRentalPrice(road, new BigDecimal(5).stripTrailingZeros());
        policy1.setDailyRentalPrice(electric, new BigDecimal(15).stripTrailingZeros());

        LocalDate yearAgo = LocalDate.now().minusYears(1);

        b1 = new Bike(mountain,yearAgo,Utils.ECondition.NEW,0);
        b2 = new Bike(road,yearAgo,Utils.ECondition.NEW,0);
        b3 = new Bike(electric,yearAgo,Utils.ECondition.NEW,0);

        bikes = Arrays.asList(b1,b2,b3);

        //2 days
        r2 = new DateRange(LocalDate.of(2019,1,1),LocalDate.of(2019,1,2));
        //3 days
        r3 = new DateRange(LocalDate.of(2019,1,1),LocalDate.of(2019,1,3));
        //6 days
        r6 = new DateRange(LocalDate.of(2019,1,1),LocalDate.of(2019,1,6));
        //7 days
        r7 = new DateRange(LocalDate.of(2019,1,1),LocalDate.of(2019,1,7));
        // 13 days
        r13 = new DateRange(LocalDate.of(2019,1,1),LocalDate.of(2019,1,13));
        // 14 days
        r14 = new DateRange(LocalDate.of(2019,1,1),LocalDate.of(2019,1,14));
        // 50 days
        r50=  new DateRange(LocalDate.of(2019,1,1),LocalDate.of(2019,2,19));
    }

    @Test
    void testNoDiscount()
    {
        BigDecimal expected = new BigDecimal(60);
        assertEquals(policy1.calculatePrice(bikes, r2).stripTrailingZeros(),expected.stripTrailingZeros());
    }

    @Test
    void testSmallDiscount()
    {
        BigDecimal expected = new BigDecimal(85.5);
        assertEquals(policy1.calculatePrice(bikes, r3).stripTrailingZeros(),expected.stripTrailingZeros());
        expected = new BigDecimal(171);
        assertEquals(policy1.calculatePrice(bikes, r6).stripTrailingZeros(),expected.stripTrailingZeros());

    }

    @Test
    void testMediumDiscount()
    {
        BigDecimal expected = new BigDecimal(189);
        assertEquals(policy1.calculatePrice(bikes, r7).stripTrailingZeros(),expected.stripTrailingZeros());
        expected = new BigDecimal(351);
        assertEquals(policy1.calculatePrice(bikes, r13).stripTrailingZeros(),expected.stripTrailingZeros());

    }
    
    @Test
    void testBigDiscount()
    {
        BigDecimal expected = new BigDecimal(357);
        assertEquals(policy1.calculatePrice(bikes, r14).stripTrailingZeros(),expected.stripTrailingZeros());
        expected = new BigDecimal(1275);
        assertEquals(policy1.calculatePrice(bikes, r50).stripTrailingZeros(),expected.stripTrailingZeros());

    }
}