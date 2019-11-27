package uk.ac.ed.bikerental;

import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.function.BooleanSupplier;

import javax.lang.model.util.ElementScanner6;

public class DateRange {
    private LocalDate start, end;
    
    public DateRange(LocalDate start, LocalDate end) {
        this.start = start;
        this.end = end;
    }
    
    public LocalDate getStart() {
        return this.start;
    }
    
    public LocalDate getEnd() {
        return this.end;
    }

    public long toYears() {
        return ChronoUnit.YEARS.between(this.getStart(), this.getEnd());
    }

    public long toDays() {
        return ChronoUnit.DAYS.between(this.getStart(), this.getEnd());
    }

    public boolean overlaps(DateRange other) {
        
        assert (other != null);
        
        // We can assume that all local dates we need to compare lie in the same time zone. 
        // As soon as we allow bike rentals to Gibraltar, Montserrat, the Pitcairn Islands or other overseas territories, we might have to 
        // rethink this implementation. But we won't do that. 

        //two dates overlap if  s2 <= s1 <= e2 or s2 <= e1 <= e2 from both points of view
        
        if (getEnd().compareTo(other.getStart()) >= 0 && other.getEnd().compareTo(getStart()) >= 0) {
        	return true;
        } else {
        	return false;
        }
        
    }

    @Override
    public int hashCode() {
        // hashCode method allowing use in collections
        return Objects.hash(end, start);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)        //check reference is the same
            return true;        
        if (obj == null)        
            return false;
        if (getClass() != obj.getClass())      //we check that the other object is the same class
            return false;

        DateRange other = (DateRange) obj;      
        return Objects.equals(end, other.end) && Objects.equals(start, other.start);        //equal only if values inside are equal
    }
    
    // You can add your own methods here
}
