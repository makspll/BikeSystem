package uk.ac.ed.bikerental;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import uk.ac.ed.bikerental.Utils.EBookingStatus;
import uk.ac.ed.bikerental.Utils.ECollectionMode;

public class TestBooking
{
    private Booking b;

    @BeforeEach
    void setUp()
    {
        b = new Booking(BigDecimal.ZERO,BigDecimal.ZERO,new ArrayList<Integer>(0),
            new DateRange(LocalDate.of(2019,1,1),LocalDate.of(2019,1,2)),
            0,ECollectionMode.DELIVERY);
    }
    @Test
    void testBookedTransitions()
    {
        b.progressBooking(false);
        //correct proggression
        assertEquals(EBookingStatus.BIKES_AWAY,b.getStatus());

        setUp();

        b.progressBooking(true);
        //correct progression
        assertEquals(EBookingStatus.DELIVERY_TO_CLIENT,b.getStatus());
    }

    @Test
    void testDeliveryTransitions()
    {
        //client
        b.setBookingStatus(EBookingStatus.DELIVERY_TO_CLIENT);
        b.progressBooking(true);
        assertEquals(EBookingStatus.BIKES_AWAY, b.getStatus());

        //provider
        b.setBookingStatus(EBookingStatus.DELIVERY_TO_PROVIDER);
        b.progressBooking(true);
        assertEquals(EBookingStatus.RETURNED, b.getStatus());
    }

    @Test
    void testBikesAwayTransitions()
    {
        b.setBookingStatus(EBookingStatus.BIKES_AWAY);
        b.progressBooking(true);
        assertEquals(EBookingStatus.DELIVERY_TO_PROVIDER, b.getStatus());

        setUp();

        b.setBookingStatus(EBookingStatus.BIKES_AWAY);
        b.progressBooking(false);
        assertEquals(EBookingStatus.RETURNED, b.getStatus());
    }
}