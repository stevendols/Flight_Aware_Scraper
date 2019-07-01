import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;

class Plane
{
    //flight name
    private String identifier;

    //all values stored in ArrayLists to accommodate varying size
    private ArrayList<LocalDate> flightDates;
    private ArrayList<String> aircraftTypes;
    private ArrayList<String> origins;
    private ArrayList<String> originCoordinates;
    private ArrayList<String> destinationCoordinates;
    private ArrayList<String> destinations;
    private ArrayList<LocalTime> departures;
    private ArrayList<LocalTime> arrivals;
    private ArrayList<Duration> durations;

    Plane(String id)
    {
        identifier = id;
        flightDates = new ArrayList<>( );
        aircraftTypes = new ArrayList<>( );
        origins = new ArrayList<>( );
        originCoordinates = new ArrayList<>( );
        destinationCoordinates = new ArrayList<>( );
        destinations = new ArrayList<>( );
        departures = new ArrayList<>( );
        arrivals = new ArrayList<>( );
        durations = new ArrayList<>( );
    }

    String getIdentifier()
    {
        return identifier;
    }


    ArrayList<LocalDate> getFlightDates()
    {
        return flightDates;
    }

    ArrayList<String> getAircraftTypes()
    {
        return aircraftTypes;
    }

    ArrayList<String> getOrigins()
    {
        return origins;
    }

    ArrayList<String> getOriginCoordinates()
    {
        return originCoordinates;
    }

    ArrayList<String> getDestinationCoordinates()
    {
        return destinationCoordinates;
    }

    ArrayList<String> getDestinations()
    {
        return destinations;
    }

    ArrayList<LocalTime> getDepartures()
    {
        return departures;
    }

    ArrayList<LocalTime> getArrivals()
    {
        return arrivals;
    }

    ArrayList<Duration> getDurations()
    {
        return durations;
    }

    int getRecordCount()
    {
        return flightDates.size( );
    }
}
