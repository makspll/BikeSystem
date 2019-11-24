package uk.ac.ed.bikerental;

import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.function.BooleanSupplier;

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

    public Boolean overlaps(DateRange other) {
        
        assert (other != null);
        
        // We use the number of days since the Java epoch day, 1970-01-01 to get an integer representation of the two days.
        // The one with the larger time passed since that day is the one that is "later".
        long endOfThis = this.end.getLong(ChronoField.EPOCH_DAY);
        long startOfOther = other.getStart().getLong(ChronoField.EPOCH_DAY);
        // We can assume that all local dates we need to compare lie in the same time zone. 
        // As soon as we allow bike rentals to Gibraltar, Montserrat, the Pitcairn Islands or other overseas territories, we might have to 
        // rethink this implementation. But we won't do that. 
        
        return (endOfThis >= startOfOther);                                     // If this date range ends before the other one begins, we return false.
        																		// Else we say that the date ranges overlap. 
    }

    @Override
    public int hashCode() {
        // hashCode method allowing use in collections
        return Objects.hash(end, start);
    }

    @Override
    public boolean equals(Object obj) {
        // equals method for testing equality in tests
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DateRange other = (DateRange) obj;
        return Objects.equals(end, other.end) && Objects.equals(start, other.start);
    }
    
    // You can add your own methods here
}
