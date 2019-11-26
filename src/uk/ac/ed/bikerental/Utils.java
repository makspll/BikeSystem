package uk.ac.ed.bikerental;

public final class Utils
{
    public enum EBikeType
    {
        HYBRID,
        ROAD,
        MOUNTAIN,
        ELECTRIC
    }

    public enum ECollectionMode
    {
        PICKUP,
        DELIVERY
    }

    public enum EBookingStatus
    {
        BOOKED,
        DELIVERY_TO_CLIENT,
        BIKES_AWAY,
        DELIVERY_TO_PROVIDER,
        RETURNED
    }

    public enum ECondition
    {
        NEW,
        GOOD,
        AVERAGE,
        BAD
    }
}