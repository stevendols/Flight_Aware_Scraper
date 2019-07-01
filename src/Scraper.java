import java.time.Duration;

public class Scraper
{
    private static final int MINUTES = 60000;

    public static void main(String[] args) throws InterruptedException
    {
        //noinspection InfiniteLoopStatement
        while (true)
        {
            MapFlights.main(new String[]{"random"});
            randomDelay( );
        }
    }

    private static void randomDelay() throws InterruptedException
    {
        //delay for between 1 and 5 minutes between each plane
        long delay = (long) (Math.random( ) * (4 * MINUTES) + (MINUTES));

        System.out.printf("Pausing for %s before next plane.....\n", calculateTime(delay));

        Thread.sleep(delay);
    }

    private static String calculateTime(long delay)
    {
        Duration d = Duration.ofMillis(delay);
        long minutes = d.toMinutes( );
        long secs = d.minusMinutes(minutes).getSeconds( );
        return minutes + ":" + (secs >= 10 ? secs : ("0" + secs));
    }
}
