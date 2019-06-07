import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;

public class FlightAware
{
    public static void main(String[] args)
    {
        Document planePage = null;
        String planeID;

        //all values stored in ArrayLists to accommodate varying size
        ArrayList<LocalDate> flightDates = new ArrayList<>( );
        ArrayList<String> aircraftTypes = new ArrayList<>( );

        try
        {
            planePage = Jsoup.connect("https://flightaware.com/live/flight/CAV470").get( );
            planePage = Jsoup.connect(planePage.location( ) + "/history").get( );
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
        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.ENGLISH);

        //collect and format dates and add to array list
        for (Element e : planePage.select("td.nowrap"))
        {
            flightDates.add(LocalDate.parse(e.text( ), df));
        }

        //collect aircraft of each flight and add to ArrayList
        for (Element e : planePage.select("td.nowrap td"))
        {
            aircraftTypes.add(e.text( ));
        }
    }
}
