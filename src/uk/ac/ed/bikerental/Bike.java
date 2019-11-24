package uk.ac.ed.bikerental;

import java.time.LocalDate;
import java.util.Objects;;

public class Bike implements Deliverable {
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
    }

    ///getters
    public int getCode() {
        return code;
    }

    public boolean isAvailable()
    {
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

    ///private parts
}