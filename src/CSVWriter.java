import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;

import static java.time.temporal.ChronoUnit.DAYS;


class CSVWriter
{
    static void write(Plane plane) throws FileNotFoundException
    {
        File dir = new File("./csv");

        if (!dir.exists( ))
        {
            //noinspection ResultOfMethodCallIgnored
            dir.mkdir( );
        }

        File file = new File(dir + "/" + plane.getIdentifier( ) + ".csv");


        PrintWriter pw = new PrintWriter(file);

        pw.println(
                "Date,Days Since,Aircraft,Origin,Origin Coordinates,Destination,Destination Coordinates,Departure,Arrival,Duration");

        int recordCount = plane.getFlightDates( ).size( );

        ArrayList<Duration> durations = plane.getDurations( );
        for (int i = 0; i < recordCount; i++)
        {
            String formattedDuration;
            formattedDuration = durations.get(i).equals(Duration.ZERO) ? "Unknown" : (durations.get(
                    i).toHours( ) + ":" + ((durations.get(i).toMinutesPart( )) >= 10 ? durations.get(
                    i).toMinutesPart( ) : "0" + durations.get(i).toMinutesPart( )));
            pw.println(
                    plane.getFlightDates( ).get(i) + "," +
                            DAYS.between(plane.getFlightDates( ).get(i).atStartOfDay( ),
                                    LocalDate.now( ).atStartOfDay( )) + "," +
                            plane.getAircraftTypes( ).get(i) + "," +
                            plane.getOrigins( ).get(i) + "," +
                            plane.getOriginCoordinates( ).get(i) + "," +
                            plane.getDestinations( ).get(i) + "," +
                            plane.getDestinationCoordinates( ).get(i) + "," +
                            plane.getDepartures( ).get(i) + "," +
                            plane.getArrivals( ).get(i) + "," +
                            formattedDuration
                      );
        }
        pw.close( );
        System.out.println("Writing to " + file.getPath( ) + " is complete");
    }
}
