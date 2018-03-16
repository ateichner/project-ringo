import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;

public class test {

   public static void main(String[] args) {
       String ip = "127.0.0.1";
       int port = 1234;
       Node a = new Node(ip, port);
       Node b = new Node(ip, port);


       System.out.println(a.compareTo(b));
   }
}