package uk.ac.ed.bikerental;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedList;


import uk.ac.ed.bikerental.Utils.EBikeType;
import uk.ac.ed.bikerental.Utils.ECondition;

public class TestBikeRentalSystem
{
  
    private BikeRentalSystem brs;
    
    @BeforeEach
    void setUp()
    {
        DeliveryServiceFactory.setupMockDeliveryService();
        LocalDate date = LocalDate.now();
        
        brs = new BikeRentalSystem(date);
        
    }
    
    @Test
    void testRegisterBikeType() {
        EBikeType hybrid = EBikeType.HYBRID;
        BigDecimal hybridRepValue = new BigDecimal(200);
        try{
            brs.registerBikeType(hybrid, hybridRepValue);
        } catch(Exception e)
        {
            assertTrue(false,"exception when registering first bike type");
        }

        assertTrue(brs.getEBikeTypes().contains(hybrid),"Bike type did not get added successfully");
        
        EBikeType alsoHybrid = EBikeType.HYBRID;
        BigDecimal higherHybridRepValue = new BigDecimal(300);
        assertThrows(Exception.class, () -> {
            brs.registerBikeType(alsoHybrid, higherHybridRepValue);
        });
        // This doesn't really test the BikeRentalSystem, but rather my understanding of equality for Enums
        assertTrue(brs.getEBikeTypes().contains(alsoHybrid),"Equality for types doesn't work like this" );
        
        EBikeType mountain = EBikeType.MOUNTAIN;
        BigDecimal mountainRepValue = new BigDecimal(500);
        try{
        brs.registerBikeType(mountain, mountainRepValue);
        } catch(Exception e)
        {
            assertTrue(false,"exception when registering second bike type");
        }
        assertTrue(brs.getEBikeTypes().contains(hybrid),"HYBRID is not in the List at this point" );
        assertTrue(brs.getEBikeTypes().contains(mountain),"MOUNTAIN has not been added" );
    }

    @Test
    void testRegisterBike()
    {

        EBikeType hybrid = EBikeType.HYBRID;
        BigDecimal hybridRepValue = new BigDecimal(200);
        BikeType hybridType = new BikeType(hybrid,hybridRepValue);
        ECondition cond = ECondition.AVERAGE;
        LocalDate madeOn = LocalDate.ofEpochDay(0); // This is a 50 year old bike, but the condition is alright

        // This first call should return an exception because we haven't registered the bike type or provider. 
        assertEquals(0,brs.getProviders().size(),"there shouldn't be any providers registered yet");
        boolean throwsException = false;
        try{
        brs.registerBike(hybridType,cond, madeOn,1);
        }catch(Exception e)
        {
            throwsException = true;
        }
        assertTrue(throwsException,"the code managed to register the bike even though there arent any providers or types");
        
        // This test is only valid if registerBikeType and registerBikeProvider are working, sadly. 

        Location loc = new Location("EH37QZ" , "42 Cool Town Road");
        StandardValuationPolicy vp = new StandardValuationPolicy((float) 0.3);
        StandardPricingPolicy pp = new StandardPricingPolicy();
        int providerID = brs.registerProvider(loc, vp, pp);
        
        BikeType bt = null;
        try{
            bt = brs.registerBikeType(hybrid, hybridRepValue);
        } catch(Exception e)
        {
            assertTrue(false, "registering bike type threw an exception");
        }
        int lastBikeID = Bike.getIDCounter();
        int bikeID = -1;
        try{
            bikeID = brs.registerBike(bt, cond, madeOn,providerID);
        } catch(Exception e)
        {
            assertTrue(false,"could not register bike");
        }
        assertEquals(lastBikeID+1,bikeID,"the bike ID wasn't incremented correctly");

        Bike b = null;
        try{
            b = brs.getProviderWithID(providerID).getBikeWithCode(bikeID);
        }catch(Exception e)
        {
            assertTrue(false,"could not find the right bike, or it wasnt registered with the right provider");
        }
        
        assertTrue(b.getBikeType().getType() == hybrid,"The type of the bike has changed" );
        assertTrue((b.getBikeType().getReplacementValue().stripTrailingZeros()).equals(hybridRepValue.stripTrailingZeros()),"The replacement value is different" );
        assertTrue(b.getCondition() == cond,"The condition of the bike has changed" );
    }
    
    @Test
    void testRegisterProvider() {
        int lastTakenID = BikeProvider.getIDCounter();
        Location loc = new Location("EH37QZ" , "42 Cool Town Road");
        StandardValuationPolicy vp = new StandardValuationPolicy(1f);
        StandardPricingPolicy pp = new StandardPricingPolicy();
        brs.registerProvider(loc, vp, pp);
        
        boolean containsID = false;

        for (BikeProvider bp: brs.getProviders()) {
            if (bp.getId() == lastTakenID +1) containsID = true; 
        }
        assertTrue(brs.getProviders().size() > 0, "no provider has been added");
        assertTrue(containsID,"BikeProvider has not been added or ID hasn't been set properly ");
        
        Location loc2 = new Location("XD3LOL" , "69 Old Town Road");
        StandardValuationPolicy vp2 = new StandardValuationPolicy((float) 0.7);
        StandardPricingPolicy pp2 = new StandardPricingPolicy();
        brs.registerProvider(loc2, vp2, pp2);
        
        containsID = true;
        for (BikeProvider bp: brs.getProviders()) {
            if (bp.getId() == lastTakenID + 2) {
                containsID = true;
                assertTrue(bp.getLocation().getAddress().equalsIgnoreCase("69 Old Town Road"),"ID is set to the wrong provider, or address was changed" );
            }
        }
        assertTrue(containsID,"second BikeProvider has not been added or ID hasn't been set properly");
    }
    
    @Test
    void testStepDateForward() {
        LocalDate ini = brs.getDate();
        brs.stepDateForward(); 
        LocalDate post = brs.getDate();
        assertTrue(ini.until(post).getDays()==1,"Date has not been advanced properly");
    }
}