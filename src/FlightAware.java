import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

public class FlightAware
{
    public static void main(String[] args)
    {
        Document planePage;
        try
        {
            planePage = Jsoup.connect("https://flightaware.com/live/flight/CAV470").get( );
            planePage = Jsoup.connect(planePage.location( ) + "/history").get( );

            //test connection - debug only
            System.out.println(planePage.location( ));
        }
        catch (IOException e)
        {
            e.printStackTrace( );
        }
    }
}
