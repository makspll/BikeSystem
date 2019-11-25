package uk.ac.ed.bikerental;

import java.time.LocalDate;
import java.util.LinkedList;
import java.util.Objects;;

public class Bike implements Deliverable {
	LinkedList<Booking> bookings;
    BikeType type;
    LocalDate manufactureDate;
    Utils.ECondition condition;
    boolean Available;
    int code;

    ///constructors

    public Bike(BikeType t, 
                LocalDate builtOn,
                Utils.ECondition cond, 
                int providerHash)
    {
        type = t;
        manufactureDate = builtOn;
        condition = cond;
        Available = true;
        code = Objects.hash(type,manufactureDate,condition,code);
        bookings = new LinkedList<Booking>();
    }

    ///getters
    public int getCode() {
        return code;
    }

    public boolean isAvailable(DateRange dates)
    {
    	// TODO: Takes a DateRange
    	assert(false);
        return Available;
    }

    public Utils.ECondition getCondition() {
        return condition;
    }

    public BikeType getBikeType() {
        return type;
    }

    ///public functionality
    @Override
    public void onPickup()
    {
        //TODO IMPLEMENT DELIVERABLE
    }
    @Override
    public void onDropoff()
    {
        //TODO IMPLEMENT DELIVERABLE
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
    	// TODO : Write this
    }

    ///private parts
}