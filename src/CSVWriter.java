import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;


class CSVWriter
{
    static void write(String plane, ArrayList<LocalDate> flightDates, ArrayList<String> aircraftTypes,
                      ArrayList<String> origins, ArrayList<String> destinations, ArrayList<LocalTime> departures,
                      ArrayList<LocalTime> arrivals, ArrayList<Duration> durations) throws FileNotFoundException
    {
        File file = new File("../csv/" + plane + ".csv");
        System.out.println(file.getAbsolutePath( ));
        PrintWriter pw = new PrintWriter(new File("csv/" + plane + ".csv"));
        pw.println("Date,Aircraft,Origin,Destination,Departure,Arrival,Duration");

        int recordCount = flightDates.size( );

        for (int i = 0; i < recordCount; i++)
        {
            pw.println(
                    flightDates.get(i) + "," +
                            aircraftTypes.get(i) + "," +
                            origins.get(i) + "," +
                            destinations.get(i) + "," +
                            departures.get(i) + "," +
                            arrivals.get(i) + "," +
                            durations.get(i)
                      );
        }
        pw.close( );
    }
}
