import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class FlightAware
{
    public static void main(String[] args)
    {
        Document planePage = null;
        Document google;
        String planeID;
        int recordCount;

        Map<String, String> coordinatesSearched = new HashMap<>( );

        //all values stored in ArrayLists to accommodate varying size
        ArrayList<LocalDate> flightDates = new ArrayList<>( );
        ArrayList<String> aircraftTypes = new ArrayList<>( );
        ArrayList<String> origins = new ArrayList<>( );
        ArrayList<String> originCoordinates = new ArrayList<>( );
        ArrayList<String> destinationCoordinates = new ArrayList<>( );
        ArrayList<String> destinations = new ArrayList<>( );
        ArrayList<LocalTime> departures = new ArrayList<>( );
        ArrayList<LocalTime> arrivals = new ArrayList<>( );
        ArrayList<Duration> durations = new ArrayList<>( );

        try
        {
            //select a random flight
            planePage = Jsoup.connect("https://flightaware.com/live/flight/kap1875").get( );
            planePage = Jsoup.connect(planePage.location( ) + "/history/500").get( );
        }
        catch (IOException e)
        {
            System.err.println("Plane Not Found, Program Will Exit");
            System.exit(-99);
        }

        //get plane ID (N-Number or flight identifier)
        //ID is the first word in a 4 word string
        planeID = planePage.select("td[align=left] h3").text( ).split(" ")[0];
        System.out.println("--------------------------------------------------");
        System.out.printf("Collecting Data for Plane: %s\n%s\n--------------------------------------------------\n",
                planeID, planePage.location( ));

        recordCount = planePage.select("td.nowrap").size( );

        //date format on FlightAware.com
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.ENGLISH);

        //collect and format dates and add to array list
        System.out.println("Collecting Flight Dates.....");
        int i = 1;
        for (Element e : planePage.select("td.nowrap"))
        {
            flightDates.add(LocalDate.parse(e.text( ), dateFormatter));
            System.out.printf("\t%d/%d: %s\n", i, recordCount, flightDates.get(i - 1));
            i++;
        }
        System.out.printf("%d Dates Collected\n--------------------------------------------------\n",
                flightDates.size( ));

        System.out.println("Collecting Aircraft Types.....");
        //collect aircraft of each flight and add to ArrayList
        i = 1;
        for (Element e : planePage.select("td.nowrap + td"))
        {
            aircraftTypes.add(e.text( ));
            System.out.printf("\t%d/%d: %s\n", i, recordCount, aircraftTypes.get(i - 1));
            i++;
        }
        System.out.printf("%d Aircraft Types Collected\n--------------------------------------------------\n",
                aircraftTypes.size( ));

        System.out.println("Collecting Flight Origins.....");
        i = 1;
        //collect origin of each flight and add to ArrayList
        for (Element e : planePage.select("td.nowrap + td + td"))
        {
            origins.add(e.text( ));
            if (!coordinatesSearched.containsKey(e.text( )))
            {
                try
                {
                    String airportCode = e.text( ).substring(e.text( ).indexOf("(") + 1, e.text( ).indexOf(")"));

                    Thread.sleep(randomDelay( ));
                    google = Jsoup.connect("https://www.google.com/search?q=" + airportCode + "+coordinates").get( );
                    String coordinates = '"' + google.selectFirst("div.Z0LcW").text( ) + '"';
                    coordinatesSearched.put(e.text( ), coordinates);
                    originCoordinates.add(coordinates);
                }
                catch (IOException | InterruptedException ex)
                {
                    ex.printStackTrace( );
                }
            }
            else
            {
                originCoordinates.add(coordinatesSearched.get(e.text( )));
            }
            System.out.printf("\t%d/%d: %s at %s\n", i, recordCount, origins.get(i - 1), originCoordinates.get(i - 1));
            i++;
        }
        System.out.printf("%d Origins Collected\n--------------------------------------------------\n",
                origins.size( ));

        System.out.println("Collecting Flight Destinations");
        i = 1;
        //collect destination of each flight and add to ArrayList
        for (Element e : planePage.select("td.nowrap + td + td + td"))
        {
            destinations.add(e.text( ));
            if (!coordinatesSearched.containsKey(e.text( )))
            {
                String airportCode = "";
                try
                {
                    airportCode = e.text( ).substring(e.text( ).indexOf("(") + 1, e.text( ).indexOf(")"));

                    Thread.sleep(randomDelay( ));
                    google = Jsoup.connect("https://www.google.com/search?q=" + airportCode + "+coordinates").get( );
                    String coordinates = '"' + google.selectFirst("div.Z0LcW").text( ) + '"';
                    coordinatesSearched.put(e.text( ), coordinates);
                    destinationCoordinates.add(coordinates);
                }
                catch (IOException | InterruptedException ex)
                {
                    ex.printStackTrace( );
                }
                catch (NullPointerException npe)
                {
                    System.out.println("Airport Code = " + airportCode);
                    System.out.println("https://www.google.com/search?q=" + airportCode + "+coordinates");
                    npe.printStackTrace( );
                    System.exit(-20);
                }
            }
            else
            {
                destinationCoordinates.add(coordinatesSearched.get(e.text( )));
            }
            System.out.printf("\t%d/%d: %s at %s\n", i, recordCount, destinations.get(i - 1),
                    destinationCoordinates.get(i - 1));
            i++;
        }
        System.out.printf("%d Destinations Collected\n--------------------------------------------------\n",
                destinations.size( ));

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mma", Locale.ENGLISH);

        System.out.println("Collecting Flight Departure Times.....");
        i = 1;
        //collect and format departure time of each flight and add to ArrayList
        for (Element e : planePage.select("td.nowrap + td + td + td + td"))
        {
            String formatted = e.text( ).replaceAll("(?>\\s[A-Z]{3})?(\\s[+\\-(].+)?", "");
            departures.add(LocalTime.parse(formatted, timeFormatter));
            System.out.printf("\t%d/%d: %s\n", i, recordCount, departures.get(i - 1));
            i++;
        }
        System.out.printf("%d Departure Times Collected\n--------------------------------------------------\n",
                departures.size( ));

        System.out.println("Collecting Flight Arrival Times.....");
        i = 1;
        //collect and format arrival time of each flight and add to ArrayList
        for (Element e : planePage.select("td.nowrap + td + td + td + td + td"))
        {
            String formatted = e.text( ).replaceAll("(?>\\s[A-Z]{3})?(\\s[+\\-(].+)?", "");
            arrivals.add(LocalTime.parse(formatted, timeFormatter));
            System.out.printf("\t%d/%d: %s\n", i, recordCount, arrivals.get(i - 1));
            i++;
        }
        System.out.printf("%d Arrival Times Collected\n--------------------------------------------------\n",
                arrivals.size( ));

        System.out.println("Collecting Flight Durations.....");
        i = 1;
        //collect and store duration of each flight and add to ArrayList
        for (Element e : planePage.select("td.nowrap + td + td + td + td + td + td"))
        {
            //Java Durations are funky
            //make sure that we have an actual duration and not any random format
            if (e.text( ).equalsIgnoreCase("Scheduled") || e.text( ).equalsIgnoreCase(
                    "Cancelled") || e.text( ).equalsIgnoreCase("En Route"))
            {
                durations.add(Duration.ZERO);
            }
            else
            {
                //split string into hours and minutes
                String[] time = e.text( ).split(":");
                Duration d = Duration.ZERO;

                //add to new Duration
                d = d.plusHours(Long.parseLong(time[0]));
                d = d.plusMinutes(Long.parseLong(time[1]));

                //add duration to array list
                durations.add(d);
                ///FORMAT: PT#H#M (PT is at start of all, then number of hours and number of minutes)
            }
            System.out.printf("\t%d/%d: %s\n", i, recordCount, durations.get(i - 1));
            i++;
        }
        System.out.printf("%d Durations Collected\n--------------------------------------------------\n",
                durations.size( ));

        System.out.println("Writing Data to File.....");
        //write flight to a CSV file
        try
        {
            CSVWriter.write(planeID, flightDates, aircraftTypes, origins, originCoordinates, destinations,
                    destinationCoordinates, departures, arrivals,
                    durations);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace( );
        }
        System.out.println("Data Collection and Storage Complete for " + planeID);
        System.out.println("--------------------------------------------------");
    }

    //delay next request for between 2 and 10 seconds
    private static int randomDelay()
    {
        return (int) (Math.random( ) * 8000) + 2000;
    }
}
