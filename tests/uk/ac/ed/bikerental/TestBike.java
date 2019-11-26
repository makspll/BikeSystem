package uk.ac.ed.bikerental;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import uk.ac.ed.bikerental.Utils.ECollectionMode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedList;

public class TestBike
{
    Bike b1;
    Booking booking,booking2;
    DateRange dr1,dr2,dr3,dr4,dr5;
    @BeforeEach
    void setUp() throws Exception
    {
        BikeType mountain = new BikeType(Utils.EBikeType.MOUNTAIN, new BigDecimal(100));
        b1 = new Bike(mountain, LocalDate.of(1998,1,12), Utils.ECondition.NEW);

        //booked
        dr1 = new DateRange(LocalDate.of(2019,1,1),
                            LocalDate.of(2019,1,7));

        //right after booked
        dr2 = new DateRange(LocalDate.of(2019,1,8),
                            LocalDate.of(2019,1,10));

        //overlaps with booked partially
        dr3 = new DateRange(LocalDate.of(2019,1,7),
                            LocalDate.of(2019,1,8));

        //right before booked
        dr4 = new DateRange(LocalDate.of(2018,12,28),
                            LocalDate.of(2018,12,31));

        //overlaps with booked partially
        dr5 = new DateRange(LocalDate.of(2018,12,31),
                            LocalDate.of(2019,1,1));

        LinkedList<Integer> bikes = new LinkedList<Integer>();
        bikes.add(b1.getCode());

        booking = new Booking(BigDecimal.ZERO,BigDecimal.ZERO,bikes,dr1,0,ECollectionMode.DELIVERY);
        booking2 = new Booking(BigDecimal.ZERO,BigDecimal.ZERO,bikes,dr2,0,ECollectionMode.DELIVERY);

        b1.addBooking(booking);
                       
    }

    @Test
    void testIsAvailableTrue()
    {
        assertEquals(true,b1.isAvailable(dr2));
        assertEquals(true,b1.isAvailable(dr4));
    }

    @Test
    void testIsAvailableFalse()
    {
        assertEquals(false,b1.isAvailable(dr1));
        assertEquals(false,b1.isAvailable(dr3));
        assertEquals(false,b1.isAvailable(dr5));
    }

    @Test
    void testOnPickupFromProvider()
    {
        //initial state is already set
        b1.onPickup();
        assertEquals(Utils.EBookingStatus.DELIVERY_TO_CLIENT,booking.getStatus());
        assertEquals(false,b1.inStore()); 
    }

    @Test
    void testOnPickupFromPartnerProvider()
    {
        //initial state before client returns to partner provider
        booking.setBookingStatus(Utils.EBookingStatus.BIKES_AWAY);
        b1.setInStore(false);
        //test
        b1.onPickup();
        assertEquals(Utils.EBookingStatus.DELIVERY_TO_PROVIDER,booking.getStatus());
        assertEquals(false,b1.inStore());
        
    }

    @Test
    void testOnDropOffToClient()
    {
        //initial state before client receives the bike
        booking.setBookingStatus(Utils.EBookingStatus.DELIVERY_TO_CLIENT);
        b1.setInStore(false);
        //test
        b1.onDropoff();
        assertEquals(Utils.EBookingStatus.BIKES_AWAY,booking.getStatus());
        assertEquals(false,b1.inStore());
    }

    @Test
    void testOnDropOffToProvider()
    {
        //initial state before bike is returned to original provider by delivery service
        booking.setBookingStatus(Utils.EBookingStatus.DELIVERY_TO_PROVIDER);
        b1.setInStore(false);
        //test
        b1.onDropoff();
        assertEquals(Utils.EBookingStatus.RETURNED,booking.getStatus());
        assertEquals(true,b1.inStore());
        //check the booking is deleted, since it becomes inactive
        //assert(b1.bookings.size() == 0);
    }
    @Test
    void testMultipleBookingsIsAvailableTrue()
    {
        b1.addBooking(booking2);
        assertEquals(true,b1.isAvailable(dr4));
    }

    @Test
    void testMultipleBookingsIsAvailableFalse()
    {
        b1.addBooking(booking2);
        assertEquals(false,b1.isAvailable(dr1));
        assertEquals(false,b1.isAvailable(dr2));
        assertEquals(false,b1.isAvailable(dr3));
        assertEquals(false,b1.isAvailable(dr5));
    }
}