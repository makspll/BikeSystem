package uk.ac.ed.bikerental;

import java.time.LocalDate;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class MockDeliveryService implements DeliveryService {
    Map<LocalDate, Collection<Deliverable>> pickups;
    Deque<Deliverable> dropoffs;
    
    public MockDeliveryService() {
        this.pickups = new HashMap<LocalDate,
                Collection<Deliverable>>();
        this.dropoffs = new ArrayDeque<Deliverable>();
    }

    @Override
    public void scheduleDelivery(
            Deliverable deliverable,
            Location pickupLocation,
            Location dropoffLocation,
            LocalDate pickupDate) {
        this.pickups.putIfAbsent(pickupDate,
                new HashSet<Deliverable>());
        this.pickups.get(pickupDate).add(deliverable);
    }
    
    public Collection<Deliverable> getPickupsOn(LocalDate date) {
        return this.pickups.get(date);
    }
    
    public void carryOutPickups(LocalDate date) {
        if (this.pickups.containsKey(date)) {
            for (Deliverable d : this.pickups.get(date)) {
                d.onPickup();
                dropoffs.add(d);
            }
            this.pickups.remove(date);
        }
    }

    public void carryOutDropoffs() {
        while (!(this.dropoffs.isEmpty())) {
            Deliverable d = this.dropoffs.pop();
            d.onDropoff();
        }
    }

    public Collection<Deliverable> getDropoffs() {
        return this.dropoffs;
    }
}
