package uk.ac.ed.bikerental;

public class DeliveryServiceFactory {
    private static DeliveryService deliveryServiceInstance;

    public static DeliveryService getDeliveryService() {
        if (deliveryServiceInstance == null) {
            // Not implemented -- we are only interested in testing using the Mock.
            assert false;
        }
        return deliveryServiceInstance;
    }

    public static void setupMockDeliveryService() {
        // Should only be called in unit tests, not production code.
        deliveryServiceInstance = new MockDeliveryService();
    }
}
