import a.d.S;

import java.net.*;
import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.*;


/**
 * Concurrent TCP server for calculating RPN values
 */
public class node {
    // globals
    private static int NUM_RINGO;
    static int NUM_ACTIVE_RINGO;
    static int PACKET_TRANSITTION_NUMBER;
    static node[] KNOWN_RINGO_LIST;
    static int[][] RTT;
    static byte[] IOQUEUE;

    // status indicator
    private static Sender sender;
    private static Receiver receiver;
    private static String flag;
    private static int PORT_NUM;

    public static void main(String[] args) {
        if (args[0].equals("ringo") && args.length == 6) {
            flag = args[1];
            PORT_NUM = Integer.parseInt(args[2]);
            String poc_name = args[3];
            int poc_port = Integer.parseInt(args[4]);
            NUM_RINGO = Integer.parseInt(args[5]);

            final ExecutorService service = Executors.newCachedThreadPool();

            while (true) {
                service.submit(new Sender(poc_name, poc_port, IOQUEUE));
            }
        } else if ((args[0].equals("offline") && args.length == 2)) {
            
        } else {
            System.out.println("invalid command, please try again !");
        }
    }

    static class Sender implements Runnable{
        String OUT_IP;
        int OUT_PORT;
        byte[] data;

        Sender(String out_ip, int out_port, byte[] data) {
            this.OUT_IP = out_ip;
            this.OUT_PORT = out_port;
            this.data = data;
        }

        public void run() {
            try {
                DatagramSocket socket = new DatagramSocket();

                // get ip destination wanted
                InetAddress IPF = InetAddress.getByName(OUT_IP);

                // send data
                for (int i = 0; i < 4; i++) {
                    byte[] out_data = Arrays.copyOfRange(data, 2500 * i, 2500 * (i + 1));
                    DatagramPacket sendPkt = new DatagramPacket(out_data, 2500, IPF, OUT_PORT);
                    socket.send(sendPkt);
                }
            } catch (SocketException e) {
                System.out.println("initializing socket failed");
            } catch (UnknownHostException e) {
                System.out.println("cannot solve the destination IP address");
            } catch (IOException e) {
                System.out.println("sending data failed");

            }
        }
    }

    static class Receiver implements Runnable {
        public Receiver() {
            // TODO
        }

        public void run() {
            // TODO
        }
    }

//     static class ServerClass implements Runnable {
//         Socket s;
//         public ServerClass(Socket socket) {
//             s = socket;
//         }
//
//         public void run() {
//             int total = 0;
//             try {
//                 DataInputStream in = new DataInputStream(s.getInputStream());
//                 DataOutputStream out = new DataOutputStream(s.getOutputStream());
//
//                 StringBuffer inputBuffer = new StringBuffer();
//
//                 rpnProtocol protocol = new rpnProtocol();
//
//                 String input;
//                 //Need some sort of loop in while condition
//                 while ((input = in.readLine()) != null) {
//                     inputBuffer.append(input);
//                     //Remove below after testing
//                     System.out.println(input);
//                 }
//
//                 String[] rpn = inputBuffer.toString().split(" ");
//                 String result = protocol.evaluateRpn(rpn);
//
//                 if (result.contains("ERROR")) {
//                     out.writeChars(result);
//
//                 } else if (result.contains("OK ANOTHER")){
//                     String[] resultArr = result.split(",");
//                     try {
//                         total = Integer.parseInt(resultArr[0]);
//                         out.writeChars("ACK ANOTHER " + total);
//                     } catch (NumberFormatException nfe1) {
//                         System.out.print("Number Format Exception: " + nfe1);
//                         out.writeChars("NACK (ERROR: MALFORMED ARGUMENT, BROKEN SERVER CODE)");
//                     }
//                 } else if (result.contains("OK END")) {
//                     String[] resultArr = result.split(",");
//                     try {
//                         total = Integer.parseInt(resultArr[0]);
//                         out.writeChars("ACK END " + total);
//
//                     } catch (NumberFormatException nfe2) {
//                         System.out.print("NUMBER FORMAT  Exception: " + nfe2);
//                         out.writeChars("NACK (ERROR: MALFORMED ARGUMENT, BROKEN SERVER CODE) FIN");
//                     }
//                 }
//                 s.close();
//             } catch (MalformedURLException me){
//                 System.out.println("Malformed URL: " + me);
//             } catch (IOException ioe) {
//                System.out.println("IOException: " + ioe);
//             }
//         }
//     }
}
