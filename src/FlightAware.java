import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

public class FlightAware
{
    public static void main(String[] args)
    {
        Document planePage = null;
        String planeID;

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
    }
}
