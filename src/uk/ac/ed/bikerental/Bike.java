package uk.ac.ed.bikerental;

import java.time.LocalDate;
import java.util.LinkedList;

import uk.ac.ed.bikerental.Utils.EBookingStatus;

public class Bike implements Deliverable {
    private LinkedList<Booking> bookings;
    private BikeType type;
    private LocalDate manufactureDate;
    private Utils.ECondition condition;
    private boolean available;
    private int code;

    static int UNIQUE_CODE_COUNTER = 0;
    ///constructors

    public Bike(BikeType t, 
                LocalDate builtOn,
                Utils.ECondition cond)
    {
        type = t;
        manufactureDate = builtOn;
        condition = cond;
        available = true;
        code = ++UNIQUE_CODE_COUNTER;
        bookings = new LinkedList<Booking>();
    }

    ///getters setters
    public static int getIDCounter()
    {
        return UNIQUE_CODE_COUNTER;
    }
    public LocalDate getManufactureDate() {
        return manufactureDate;
    }

    public int getCode() {
        return code;
    }

    public void setInStore(boolean yesno)
    {
        available = yesno;
    }
    public boolean inStore()
    {
        return available;
    }

    public Utils.ECondition getCondition() {
        return condition;
    }

    public BikeType getBikeType() {
        return type;
    }

    ///public functionality
    public boolean isAvailable(DateRange dates)
    {
        //we go through the bookings, and look for overlaps
        for(Booking b : bookings)
        {
            if(b.getDates().overlaps(dates))
            {
                //check the booking is non-active
                if(b.getStatus() == Utils.EBookingStatus.RETURNED)
                {
                    removeBooking(b);
                }
                else 
                {
                    //if we found an overlapping and active booking, we are not available
                    return false;
                }
                
            }
        }
        //we didn't find conflicts
        return true;
    }

    @Override
    public void onPickup()
    {
        //pickups can never lead to a returned state, so we don't remove bookings here

        //we find the first booking time-wise
        Booking earliest = earliestBooking();
        //we progress the earliest booking, if another bike hasn't done it already
        if(earliest.getStatus().equals(EBookingStatus.BOOKED) || earliest.getStatus().equals(EBookingStatus.BIKES_AWAY))
        {
            earliest.progressBooking(true);
        }
        //we always change the state of the bike
        available = false;
    }
    @Override
    public void onDropoff()
    {
        //dropoffs could lead to a returned state, here we delete the booking from our list 
        Booking earliest = earliestBooking();
        //we progress the booking only if another bike hasn't done it yet
        if(earliest.getStatus().equals(EBookingStatus.DELIVERY_TO_CLIENT) || earliest.getStatus().equals(EBookingStatus.DELIVERY_TO_PROVIDER))
        {
            earliest.progressBooking(true);
            
            if(earliest.getStatus() == Utils.EBookingStatus.RETURNED)
            {
                removeBooking(earliest);
                
            }
        }
        if(earliest.getStatus() == Utils.EBookingStatus.RETURNED)
        {
            available = true;
        }
    }
    @Override
    public int hashCode() {
        return code;
    }

    @Override
    public boolean equals(Object obj)
    {
        if(obj == this)
        {
            return true;
        }
        else if(obj == null)
        {
            return false;
        }
        else if(getClass() != obj.getClass())
        {
            return false;
        }

        return (code == ((Bike)obj).getCode());
    }
    
    public void addBooking(Booking b) {
        bookings.addLast(b);
    }
    
    public void removeBooking(Booking b) {
        assert(bookings.contains(b));
        bookings.remove(b);
    }

    public boolean containsBooking(Booking b)
    {
        return bookings.contains(b);
    }

    ///private parts

    //returns the earliest booking
    private Booking earliestBooking()
    {
        Booking earliest = bookings.getFirst();
        LocalDate earliestDate = LocalDate.of(3000,1,1);
        for(Booking b : bookings)
        {
            if(b == earliest){continue;}
            //that would lead to an overlapping, we don't allow that
            assert(b.getDates().getStart().compareTo(earliest.getDates().getStart()) != 0);
            //we are assuming only active bookings are in the list
            assert(b.getStatus() != Utils.EBookingStatus.RETURNED);

            if(b.getDates().getStart().compareTo(earliestDate) < 0)
            {
                earliestDate = b.getDates().getStart();
                earliest = b;
            }
        }
        return earliest;
    }
}