package uk.ac.ed.bikerental;

import java.util.List;
import java.math.BigDecimal;
import java.time.LocalDate;

public class Invoice
{
    int orderCode;
    String orderSummary;
    BigDecimal deposit;
    BigDecimal price;
    List<Integer> bikeCodes;
    Location returnLocation;
    Location pickupLocation;
    Utils.ECollectionMode collectionMode;
    LocalDate returnDate;


    ///constructors
    public Invoice(Booking b, String summary,
                    Location returnL,
                    Location pickupL,
                    Utils.ECollectionMode cMode)
    {
        orderCode = b.getOrderCode();
        orderSummary = summary;
        deposit = b.getDeposit();
        price = b.getPrice();
        bikeCodes = b.getBikeCodes();
        returnLocation = returnL;
        pickupLocation = pickupL;
        collectionMode = cMode;
        returnDate = b.getDates().getEnd();
    }
    ///getters

    public int getOrderCode()
    {
        return orderCode;
    }

    public String getOrderSummary()
    {
        return orderSummary;
    }

    public BigDecimal getDeposit()
    {
        return deposit;
    }

    public BigDecimal getPrice()
    {
        return price;
    }

    public List<Integer> getBikeCodes()
    {
        return bikeCodes;
    }

    public Location getReturnLocation()
    {
        return returnLocation;
    }

    public Location getPickupLocation()
    {
        return pickupLocation;
    }

    public Utils.ECollectionMode getCollectionMode()
    {
        return collectionMode;
    }

    public LocalDate getReturnDate()
    {
        return returnDate;
    }

    ///private parts
}