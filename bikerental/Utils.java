package uk.ac.ed.bikerental;

public class Utils
{
    enum EBikeType
    {
        HYBRID,
        ROAD,
        MOUNTAIN,
        ELECTRIC
    }

    enum ECollectionMode
    {
        PICKUP,
        DELIVERY
    }

    enum EBookingStatus
    {
        BOOKED,
        PICKED_UP,
        BIKES_ON_WAY_BACK,
        RETURNED
    }

    enum ECondition
    {
        NEW,
        GOOD,
        AVERAGE,
        BAD
    }

    public class QuoteInformation
    {
        public String name,surname,phone;
        public Location address;
        public ECollectionMode collectionMode;
    }
}