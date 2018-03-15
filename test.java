import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class test {
    public static void main(String[] args) {

    }


    private static float calculate_rtt(String ip_address) {
        float start = -1;
        float rtt = -1;
        float stop = -1;
        //Create IP UDP connection
        try {
            InetAddress ip = InetAddress.getByName(ip_address);
            boolean reachale = ip.isReachable(5000);

            start = (float) System.currentTimeMillis();
            //Send an ICMP packet
            stop = (float) System.currentTimeMillis();
            rtt = stop - start;
        } catch (UnknownHostException e) {
            System.out.println("Unknown host!");
        } catch (IOException e) {
            System.out.println("IOException!");
        }
        return rtt;
    }
}
