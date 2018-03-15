import java.io.IOException;
import java.net.InetAddress;
import java.util.GregorianCalendar;

public class test {
    public static void main(String[] args) {
        float a = calculate_rtt("me.zijinluo.com");
        System.out.println(a);
    }

    private static float calculate_rtt(String ip_address) {
        try {
            float start = new GregorianCalendar().getTimeInMillis();
            Process p1 = java.lang.Runtime.getRuntime().exec("ping -c 1 " + ip_address);
            int i = p1.waitFor();
            float end = new GregorianCalendar().getTimeInMillis();
            return end - start;
        } catch (IOException e) {

        } catch (InterruptedException e) {

        }
        return -1;
    }
}
