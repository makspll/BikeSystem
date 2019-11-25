package uk.ac.ed.bikerental;

import java.utils.List;

public class Invoice
{
    int orderCode;
    String orderSummary;
    BigDecimal deposit;
    BigDecimal price;
    List<int> bikeCodes;
    Location returnLocation;
    Location pickupLocation;
    Utils.EColllectionMode collectionMode;
    LocalDate returnDate;


    ///constructors
    public Invoice(int oc, String summary,
                    BigDecimal dep,
                    BigDecimal pri,
                    List<Int> bikes,
                    Location returnL,
                    Location pickupL,
                    Utils.EColllectionMode cMode,
                    LocalDate returnBy)
    {
        orderCode = oc;
        orderSummary = summary;
        deposit = dep;
        price = pri;
        bikeCodes = bikes;
        returnLocation = returnL;
        pickupLocation = pickupL;
        collectionMode = cMode;
        returnDate = returnBy;
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

    public List<int> getBikeCodes()
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

    public Utils.EColllectionMode getCollectionMode()
    {
        return collectionMode;
    }

    public LocalDate getReturnDate()
    {
        return returnDate;
    }

    ///private parts
}