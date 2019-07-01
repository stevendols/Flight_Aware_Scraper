import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class MapFlights
{
    private static Plane p;
    private static Map<String, Integer> visitCount;
    private static Map<String, Integer> routeCount;
    private static Map<String, String> nameHash;

    public static void main(String[] args)
    {
        String plane;
        visitCount = new HashMap<>( );
        routeCount = new HashMap<>( );
        nameHash = new HashMap<>( );

        if (args.length == 0)
        {
            Scanner scan = new Scanner(System.in);
            System.out.println("Enter Tail Number or R for random plane");
            String input = scan.next( );
            plane = input.equalsIgnoreCase("R") ? "random" : input;
        }
        else
        {
            plane = args[0];
        }

        p = FlightAware.collectFlightData(plane);

        createKML( );
    }

    private static void prepareToPlot()
    {
        for (int i = 0; i < p.getRecordCount( ); i++)
        {
            visitCount.merge(translateCoordinates(p.getOriginCoordinates( ).get(i)), 1, Integer::sum);
            nameHash.put(translateCoordinates(p.getOriginCoordinates( ).get(i)), p.getOrigins( ).get(i));

            visitCount.merge(translateCoordinates(p.getDestinationCoordinates( ).get(i)), 1, Integer::sum);
            nameHash.put(translateCoordinates(p.getDestinationCoordinates( ).get(i)), p.getDestinations( ).get(i));

            routeCount.merge(
                    translateCoordinates(p.getOriginCoordinates( ).get(i)) + " " + translateCoordinates(
                            p.getDestinationCoordinates( ).get(i)), 1, Integer::sum);
        }
    }

    private static void createKML()
    {
        File dir = new File("./kml");
        if (!dir.exists( ))
        {
            //noinspection ResultOfMethodCallIgnored
            dir.mkdir( );
        }

        File file = new File(dir + "/" + p.getIdentifier( ) + ".kml");

        try
        {
            PrintWriter kml = new PrintWriter(file);
            //Mandatory KML Header
            kml.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<kml xmlns=\"http://www.opengis.net/kml/2.2\">");

            //Start of Document
            kml.println("\t<Document>");
            kml.printf("\t\t<name>%s</name>\n\t\t<open>1</open>", p.getIdentifier( ));
            kml.println( );

            prepareToPlot( );

            //create Placemark for each unique airport
            for (String key : visitCount.keySet( ))
            {
                kml.printf("\t\t<Placemark>\n" +
                                "\t\t\t<name>%s</name>\n" +
                                "\t\t\t<description>%s</description>\n" +
                                "\t\t\t<Point>\n" +
                                "\t\t\t\t<coordinates>\n" +
                                "\t\t\t\t\t%s\n" +
                                "\t\t\t\t</coordinates>\n" +
                                "\t\t\t</Point>\n" +
                                "\t\t\t<Style>\n" +
                                "\t\t\t\t<IconStyle>\n" +
                                "\t\t\t\t\t<scale>%s</scale>\n" +
                                "\t\t\t\t\t<color>%s</color>\n" +
                                "\t\t\t\t\t<Icon>\n" +
                                "\t\t\t\t\t\t<href>http://maps.google.com/mapfiles/kml/pushpin/wht-pushpin.png</href>\n" +
                                "\t\t\t\t\t</Icon>\n" +
                                "\t\t\t\t</IconStyle>\n" +
                                "\t\t\t</Style>\n" +
                                "\t\t</Placemark>\n", nameHash.get(key) + " - " + visitCount.get(key),
                        visitCount.get(key) + " flights visited this airport\n\n" + parseFlights(nameHash.get(key)),
                        key, 0.5 * visitCount.get(key), "#FF00FF00");
            }

            //create Placemark for each route
            for (String key : routeCount.keySet( ))
            {
                kml.printf("\t\t<Placemark>\n" +
                                "\t\t\t<name>%s</name>\n" +
                                "\t\t\t<description>%s</description>\n" +
                                "\t\t\t<LineString>\n" +
                                "\t\t\t<extrude>1</extrude>\n" +
                                "\t\t\t<tesselate>1</tesselate>\n" +
                                "\t\t\t\t<coordinates>\n" +
                                "\t\t\t\t\t%s\n" +
                                "\t\t\t\t</coordinates>\n" +
                                "\t\t\t</LineString>\n" +
                                "\t\t\t<Style>\n" +
                                "\t\t\t\t<LineStyle>\n" +
                                "\t\t\t\t\t<width>%s</width>\n" +
                                "\t\t\t\t\t<color>%s</color>\n" +
                                "\t\t\t\t</LineStyle>\n" +
                                "\t\t\t</Style>\n" +
                                "\t\t</Placemark>\n", "", routeCount.get(key) + " flights traveled this route\n\n", key,
                        routeCount.get(key), "#FFFFFFFF");
            }

            kml.printf("\t</Document>\n" +
                    "</kml>");

            kml.close( );

        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace( );
        }
    }

    private static String parseFlights(String airportName)
    {
        StringBuilder toReturn = new StringBuilder( );

        for (int i = 0; i < p.getRecordCount( ); i++)
        {
            if (airportName.equalsIgnoreCase(p.getOrigins( ).get(i)))
            {
                LocalTime t = p.getDepartures( ).get(i);

                toReturn.append("Origin - ").append(p.getFlightDates( ).get(
                        i)).append(t == null ? " - Unknown Time\n" : " at " + p.getDepartures( ).get(i) + "\n");
            }
            else if (airportName.equalsIgnoreCase(p.getDestinations( ).get(i)))
            {
                LocalTime t = p.getArrivals( ).get(i);
                toReturn.append("Dest - ").append(p.getFlightDates( ).get(
                        i)).append(t == null ? " - Unknown Time\n" : " at " + p.getArrivals( ).get(i) + "\n");
            }
        }

        return toReturn.toString( );
    }

    //TODO handle blank coordinates
    private static String translateCoordinates(String s)
    {
        String latitude = s.substring(0, s.indexOf(","));
        if (latitude.contains("S"))
        {
            latitude = "-" + latitude;
        }
        latitude = latitude.replaceAll("[^.\\d-]", "");

        String longitude = s.substring(s.indexOf(" ") + 1);
        if (longitude.contains("W"))
        {
            longitude = "-" + longitude;
        }
        longitude = longitude.replaceAll("[^.\\d-]", "");

        return longitude + "," + latitude;
    }
}
