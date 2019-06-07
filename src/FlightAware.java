import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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

        //collect raw dates from web page
        Elements rawDates = planePage.select("td.nowrap");
        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.ENGLISH);

        //format dates correctly and add to array list
        for (Element e : rawDates)
        {
            flightDates.add(LocalDate.parse(e.text( ), df));
        }
    }
}
