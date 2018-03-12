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
    static ArrayList<String[]> KNOWN_RINGO_LIST = new ArrayList<>();
    static int[][] RTT;
    static Queue<byte[]> IO_QUEUE = new ArrayDeque<>();

    // status indicator
    private static Sender sender;
    private static Receiver receiver;
    private static String flag;
    private static int PORT_NUM;

    public static void main(String[] args) {
        final ExecutorService service = Executors.newCachedThreadPool();

        while (true) {
            Scanner scanner = new Scanner(System.in);
            String[] in = scanner.nextLine().trim().split(" ");

            if (in.length == 6 && in[0].equals("ringo")) {
                flag = in[1];
                PORT_NUM = Integer.parseInt(in[2]);
                String poc_name = in[3];
                String poc_port_str = in[4];
                int poc_port = Integer.parseInt(poc_port_str);
                add_poc(poc_name, poc_port_str);
                NUM_RINGO = Integer.parseInt(in[5]);

            } else if (in.length == 2 && in[0].equals("offline")) {
                try {
                    Thread.sleep(Integer.parseInt(in[1]));
                } catch (InterruptedException e) {
                    System.out.println("node offline failed");
                }

            } else {
                System.out.println("invalid command, please try again !");
            }

            scanner.close();
        }
    }

    /**
     * the private sender class
     *
     * in charge of sending data
     */
    static class Sender implements Runnable{
        String OUT_IP;
        int OUT_PORT;

        Sender(String out_ip, int out_port) {
            this.OUT_IP = out_ip;
            this.OUT_PORT = out_port;
        }

        public void run() {
            try {
                DatagramSocket socket = new DatagramSocket();

                // get ip destination wanted
                InetAddress IPF = InetAddress.getByName(OUT_IP);

                // send data
                while (true) {
                    if (IO_QUEUE.size() != 0) {
                        byte[] out_data = IO_QUEUE.poll();
                        DatagramPacket sendPkt = new DatagramPacket(out_data, 2500, IPF, OUT_PORT);
                        socket.send(sendPkt);
                    }
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

    static class Manager implements Runnable {
        Manager() {

        }

        public void run() {

        }
    }


    /**
     * add poc to known list if possible
     * @param poc_name the IP address or DNS name of poc
     * @param poc_port the port number of poc
     */
    private static void add_poc(String poc_name, String poc_port) {
        for (int i = 0; i < KNOWN_RINGO_LIST.size(); i++) {
            if (KNOWN_RINGO_LIST.get(i)[0].equals(poc_name)) {
                System.out.println(poc_name + " is already in known_list");
                break;
            } else {
                KNOWN_RINGO_LIST.add(new String[] {poc_name, poc_port});
            }

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
