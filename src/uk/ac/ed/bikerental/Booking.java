package uk.ac.ed.bikerental;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import uk.ac.ed.bikerental.Utils.EBookingStatus;
import uk.ac.ed.bikerental.Utils.ECollectionMode;

public class Booking {
    private int orderCode;
    private BigDecimal deposit;
    private BigDecimal price;
    private EBookingStatus status;
    private List<Integer> bikeCodes;
    private DateRange dates;
    private int providerID;
    private ECollectionMode collectionMode;
    private static int UNIQUE_CODE_COUNT = 0;
    
    ///getters setters
    public DateRange getDates() {return dates;}
    public int getOrderCode() { return orderCode; }
    public EBookingStatus getStatus() { return status; }
    public BigDecimal getDeposit() {return deposit;}
    public BigDecimal getPrice() {return price;}
    public List<Integer> getBikeCodes() {return bikeCodes;}
    public int getProviderID() {return providerID;}
    public ECollectionMode getCollectionMode() {return collectionMode;}
    public void setBookingStatus(EBookingStatus newStatus) {status = newStatus;}

    ///public functions
    public Booking(BigDecimal pDeposit, BigDecimal pPrice , List<Integer> pBikeCodes , DateRange Dates, int pProviderID, ECollectionMode collMode) {
        orderCode = ++UNIQUE_CODE_COUNT;									// This might work for now. 
        deposit = pDeposit;
        price = pPrice;
        status = EBookingStatus.BOOKED;
        bikeCodes = pBikeCodes;
        dates = Dates;
        providerID = pProviderID;
        collectionMode = collMode;
    }	
    

    public static int getIDCounter()
    {
        return UNIQUE_CODE_COUNT;
    }
    public void progressBooking(boolean progressedByDeliveryService) {
        //FSM for the bookings, it can always go 2 ways (when active), except when it's in delivery, then there's only one way
        //each booking can only be updated once per delivery, so not every bike should update the booking

        switch (status) {
            case BOOKED: 
                status = (progressedByDeliveryService)?Utils.EBookingStatus.DELIVERY_TO_CLIENT: Utils.EBookingStatus.BIKES_AWAY;
                break;
            case DELIVERY_TO_CLIENT:
                //it better be updated only by the delivery service
                assert(progressedByDeliveryService == true);
                status = Utils.EBookingStatus.BIKES_AWAY;
                break;
            case BIKES_AWAY:
                status = (progressedByDeliveryService)?Utils.EBookingStatus.DELIVERY_TO_PROVIDER: Utils.EBookingStatus.RETURNED;
                break;
            case DELIVERY_TO_PROVIDER:
                assert(progressedByDeliveryService == true);
                status = Utils.EBookingStatus.RETURNED;
                break;
            default:
                //we cannot progress an inactive booking
                assert(false);
                break;
        }
    }

    ///private parts
}
