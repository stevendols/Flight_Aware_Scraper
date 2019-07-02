import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

class FlightAware
{
    static Plane collectFlightData(String flight)
    {
        Document planePage = null;
        Document google;
        String planeID;
        int recordCount;

        Map<String, String> coordinatesSearched = new HashMap<>( );

        try
        {
            //select a random flight
            planePage = Jsoup.connect("https://flightaware.com/live/flight/" + flight).get( );
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
        Plane plane = new Plane(planeID);

        System.out.println("--------------------------------------------------");
        System.out.printf("Collecting Data for Plane: %s\n%s\n--------------------------------------------------\n",
                plane.getIdentifier( ), planePage.location( ));

        recordCount = planePage.select("td.nowrap").size( );

        //date format on FlightAware.com
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.ENGLISH);

        //collect and format dates and add to array list
        System.out.println("Collecting Plane Dates.....");
        int i = 1;
        for (Element e : planePage.select("td.nowrap"))
        {
            plane.getFlightDates( ).add(LocalDate.parse(e.text( ), dateFormatter));
            System.out.printf("\t%d/%d: %s\n", i, recordCount, plane.getFlightDates( ).get(i - 1));
            i++;
        }
        System.out.printf("%d Dates Collected\n--------------------------------------------------\n",
                plane.getFlightDates( ).size( ));

        System.out.println("Collecting Aircraft Types.....");
        //collect aircraft of each flight and add to ArrayList
        i = 1;
        for (Element e : planePage.select("td.nowrap + td"))
        {
            plane.getAircraftTypes( ).add(e.text( ));
            System.out.printf("\t%d/%d: %s\n", i, recordCount, plane.getAircraftTypes( ).get(i - 1));
            i++;
        }
        System.out.printf("%d Aircraft Types Collected\n--------------------------------------------------\n",
                plane.getAircraftTypes( ).size( ));

        Scanner scanner = new Scanner(System.in);

        System.out.println("Collecting Plane Origins.....");
        i = 1;
        //collect origin of each flight and add to ArrayList
        for (Element e : planePage.select("td.nowrap + td + td"))
        {
            plane.getOrigins( ).add(e.text( ));
            if (!coordinatesSearched.containsKey(e.text( )))
            {
                String coordinates = "";
                String airportCode;
                try
                {
                    airportCode = e.text( ).substring(e.text( ).indexOf("(") + 1, e.text( ).indexOf(")"));

                    Thread.sleep(randomDelay( ));
                    google = Jsoup.connect(
                            "https://www.google.com/search?q=" + airportCode + "+airport + coordinates").get( );
                    coordinates = '"' + google.selectFirst("div.kp-header").text( ) + '"';
                }
                catch (NullPointerException | StringIndexOutOfBoundsException ex)
                {
                    String full = "";
                    try
                    {
                        full = e.text( ).replace("Near ", "");
                        Thread.sleep(randomDelay( ));
                        google = Jsoup.connect(
                                "https://www.google.com/search?q=" + full + "+coordinates").get( );
                        coordinates = '"' + google.selectFirst("div.kp-header").text( ) + '"';
                    }
                    catch (Exception x)
                    {
                        System.out.printf("Automatic Lookup failed, please enter coordinates for %s\n>", full);
                        coordinates = '"' + scanner.nextLine( ) + '"';
                    }
                }
                catch (IOException | InterruptedException ex)
                {
                    ex.printStackTrace( );
                }
                finally
                {
                    coordinatesSearched.put(e.text( ), coordinates);
                    plane.getOriginCoordinates( ).add(coordinates);
                    System.out.printf("\t%d/%d: %s at %s\n", i, recordCount, plane.getOrigins( ).get(i - 1),
                            plane.getOriginCoordinates( ).get(i - 1));
                }
            }
            else
            {
                plane.getOriginCoordinates( ).add(coordinatesSearched.get(e.text( )));
                System.out.printf("\t%d/%d: %s at %s\n", i, recordCount, plane.getOrigins( ).get(i - 1),
                        plane.getOriginCoordinates( ).get(i - 1));
            }
            i++;
        }
        System.out.printf("%d Origins Collected\n--------------------------------------------------\n",
                plane.getOrigins( ).size( ));

        System.out.println("Collecting Plane Destinations");
        i = 1;

        //collect destination of each flight and add to ArrayList
        for (Element e : planePage.select("td.nowrap + td + td + td"))
        {
            plane.getDestinations( ).add(e.text( ));
            if (!coordinatesSearched.containsKey(e.text( )))
            {
                String airportCode = "", coordinates = "";
                try
                {
                    airportCode = e.text( ).substring(e.text( ).indexOf("(") + 1, e.text( ).indexOf(")"));

                    Thread.sleep(randomDelay( ));
                    google = Jsoup.connect("https://www.google.com/search?q=" + airportCode + "+coordinates").get( );
                    coordinates = '"' + google.selectFirst("div.kp-header").text( ) + '"';
                }
                catch (NullPointerException npe)
                {
                    System.out.printf("Automatic Lookup failed, please enter coordinates for %s\n>", airportCode);
                    coordinates = '"' + scanner.nextLine( ) + '"';
                }
                catch (IOException | InterruptedException ex)
                {
                    ex.printStackTrace( );
                }
                finally
                {
                    coordinatesSearched.put(e.text( ), coordinates);
                    plane.getDestinationCoordinates( ).add(coordinates);
                    System.out.printf("\t%d/%d: %s at %s\n", i, recordCount, plane.getDestinations( ).get(i - 1),
                            plane.getDestinationCoordinates( ).get(i - 1));
                }
            }
            else
            {
                plane.getDestinationCoordinates( ).add(coordinatesSearched.get(e.text( )));
                System.out.printf("\t%d/%d: %s at %s\n", i, recordCount, plane.getDestinations( ).get(i - 1),
                        plane.getDestinationCoordinates( ).get(i - 1));
            }
            i++;
        }
        System.out.printf("%d Destinations Collected\n--------------------------------------------------\n",
                plane.getDestinations( ).size( ));


        System.out.println("Collecting Plane Durations.....");
        i = 1;
        //collect and store duration of each flight and add to ArrayList
        for (Element e : planePage.select("td.nowrap + td + td + td + td + td + td"))
        {
            //Java Durations are funky
            //make sure that we have an actual duration and not any random format
            if (e.text( ).equalsIgnoreCase("Scheduled") || e.text( ).equalsIgnoreCase(
                    "Cancelled") || e.text( ).equalsIgnoreCase("En Route") || e.text( ).equalsIgnoreCase("Diverted"))
            {
                plane.getDurations( ).add(Duration.ZERO);
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
                plane.getDurations( ).add(d);
                ///FORMAT: PT#H#M (PT is at start of all, then number of hours and number of minutes)
            }
            System.out.printf("\t%d/%d: %s\n", i, recordCount, plane.getDurations( ).get(i - 1));
            i++;
        }
        System.out.printf("%d Durations Collected\n--------------------------------------------------\n",
                plane.getDurations( ).size( ));

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mma", Locale.ENGLISH);

        System.out.println("Collecting Plane Departure Times.....");
        i = 1;
        //collect and format departure time of each flight and add to ArrayList
        for (Element e : planePage.select("td.nowrap + td + td + td + td"))
        {
            if (e.text( ).equals(""))
            {
                plane.getDepartures( ).add(null);
            }
            else
            {
                String formatted = e.text( ).replaceAll("(?>\\s[A-Z]{3,4})?(\\s[+\\-(].+)?", "");
                plane.getDepartures( ).add(LocalTime.parse(formatted, timeFormatter));
            }
            System.out.printf("\t%d/%d: %s\n", i, recordCount, plane.getDepartures( ).get(i - 1));
            i++;
        }
        System.out.printf("%d Departure Times Collected\n--------------------------------------------------\n",
                plane.getDepartures( ).size( ));

        System.out.println("Collecting Plane Arrival Times.....");
        i = 1;
        //collect and format arrival time of each flight and add to ArrayList
        for (Element e : planePage.select("td.nowrap + td + td + td + td + td"))
        {
            if (e.text( ).equals(""))
            {
                plane.getArrivals( ).add(null);
            }
            else
            {
                String formatted = e.text( ).replaceAll("(?>\\s[A-Z]{3,4})?(\\s[+\\-(].+)?", "");
                plane.getArrivals( ).add(LocalTime.parse(formatted, timeFormatter));
            }
            System.out.printf("\t%d/%d: %s\n", i, recordCount, plane.getArrivals( ).get(i - 1));
            i++;
        }
        System.out.printf("%d Arrival Times Collected\n--------------------------------------------------\n",
                plane.getArrivals( ).size( ));
        System.out.println("Writing Data to File.....");
        //write flight to a CSV file
        try
        {
            CSVWriter.write(plane);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace( );
        }
        System.out.println("Data Collection and Storage Complete for " + planeID);
        System.out.println("--------------------------------------------------");

        return plane;
    }

    //delay next request for between 2 and 10 seconds
    private static int randomDelay()
    {
        return (int) (Math.random( ) * 8000) + 2000;
    }

}