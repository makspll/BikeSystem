package uk.ac.ed.bikerental;

import org.junit.jupiter.api.*;

class TestLocation {
    Location location1, location2, location3;
    @BeforeEach
    void setUp() throws Exception {
        location1 = new Location("EJ23 923","23 Torries Road");  // my house
        location2 = new Location("EH23 923","404 Found Street"); // my mom's house
        location3 = new Location("EH13 923","kama oaprah 4/12");  // sum neighbour
    }
    
    @Test()
    void testNearLocations()
    {
        assert(location2.isNearTo(location3));
        assert(location3.isNearTo(location2));
    }

    @Test()
    void testFarLocations()
    {
        assert(!location1.isNearTo(location2));
        assert(!location1.isNearTo(location3));

        assert(!location2.isNearTo(location1));
        assert(!location3.isNearTo(location1));
    }
}
