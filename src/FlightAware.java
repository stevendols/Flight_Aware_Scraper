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
import java.util.Locale;

public class FlightAware
{
    public static void main(String[] args)
    {
        Document planePage = null;
        Document google;
        String planeID;

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
            planePage = Jsoup.connect("https://flightaware.com/live/flight/n14053").get( );
            planePage = Jsoup.connect(planePage.location( ) + "/history/500").get( );
        }
        catch (IOException e)
        {
            e.printStackTrace( );
            System.err.println("Plane Not Found, Program Will Exit");
            System.exit(-1);
        }

        //get plane ID (N-Number or flight identifier)
        //ID is the first word in a 4 word string
        planeID = planePage.select("td[align=left] h3").text( ).split(" ")[0];
        System.out.println(planeID);

        //date format on FlightAware.com
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.ENGLISH);

        //collect and format dates and add to array list
        for (Element e : planePage.select("td.nowrap"))
        {
            flightDates.add(LocalDate.parse(e.text( ), dateFormatter));
        }

        //collect aircraft of each flight and add to ArrayList
        for (Element e : planePage.select("td.nowrap + td"))
        {
            aircraftTypes.add(e.text( ));
        }

        //collect origin of each flight and add to ArrayList
        for (Element e : planePage.select("td.nowrap + td + td"))
        {
            origins.add(e.text( ));
            try
            {
                String airportCode = e.text( ).substring(e.text( ).indexOf("(") + 1, e.text( ).indexOf(")"));

                Thread.sleep(randomDelay( ));
                google = Jsoup.connect("https://www.google.com/search?q=" + airportCode + "+coordinates").get( );
                String coordinates = '"' + google.selectFirst("div.Z0LcW").text( ) + '"';
                originCoordinates.add(coordinates);
            }
            catch (IOException | InterruptedException ex)
            {
                ex.printStackTrace( );
            }
        }

        //collect destination of each flight and add to ArrayList
        for (Element e : planePage.select("td.nowrap + td + td + td"))
        {
            destinations.add(e.text( ));
            try
            {
                String airportCode = e.text( ).substring(e.text( ).indexOf("(") + 1, e.text( ).indexOf(")"));

                Thread.sleep(randomDelay( ));
                google = Jsoup.connect("https://www.google.com/search?q=" + airportCode + "+coordinates").get( );

                String coordinates = '"' + google.selectFirst("div.Z0LcW").text( ) + '"';
                destinationCoordinates.add(coordinates);
            }
            catch (IOException | InterruptedException ex)
            {
                ex.printStackTrace( );
            }
        }

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mma z");

        //collect and format departure time of each flight and add to ArrayList
        for (Element e : planePage.select("td.nowrap + td + td + td + td"))
        {
            String formatted = e.text( ).replaceAll("\\s[+\\-(].+", "");
            departures.add(LocalTime.parse(formatted, timeFormatter));
        }

        //collect and format arrival time of each flight and add to ArrayList
        for (Element e : planePage.select("td.nowrap + td + td + td + td + td"))
        {
            String formatted = e.text( ).replaceAll("\\s[+\\-(].+", "");
            arrivals.add(LocalTime.parse(formatted, timeFormatter));
        }

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
        }

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
    }

    private static int randomDelay()
    {
        return (int) (Math.random( ) * 1000) + 2000;
    }
}
