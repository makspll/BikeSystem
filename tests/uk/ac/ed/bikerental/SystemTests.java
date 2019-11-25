package uk.ac.ed.bikerental;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

public class SystemTests {
    // You can add attributes here
    DeliveryService ds;
    BikeRentalSystem bikeSystem;
    BikeProvider bp1,bp2;
    Customer customer;
    DateRange available;
    @BeforeEach
    void setUp() throws Exception {
        // Setup mock delivery service before each tests
        DeliveryServiceFactory.setupMockDeliveryService();
        ds = DeliveryServiceFactory.getDeliveryService();
        bp1 = new BikeProvider(0,new Location("EH10 1SY","69 cumming street"), 1,"981293819238","24/7");
        bp2 = new BikeProvider(1,new Location("EH11 2S3","5R TestBased Street"),1,"12381819312","24/7");
        
        available = new DateRange(new LocalDate(2019,1,1),
                                new LocalDate(2019,1,10));

        bp1.registerBikeType();
        bp2.registerBikeType();

        bp1.registerBike();
        bp2.registerBike();

        customer = new Customer();
        customer.bikeSystem = bikeSystem;
        
    }
    
    @Test
    void findingQuoteSuccess()
    {
        
        customer.findQuotes();
    }

}
