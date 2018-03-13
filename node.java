import java.net.*;
import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.*;

/**
 * node class responsible for defining a Ringo node
 */
public class node {
    private int PORT_NUM;
    private String poc_name;
    private int NUM_RINGO;

    private HashMap<int,long> neighbor_map;
    private HashMap<String, int> poc_to_ringo_num;
    private HashMap<String, int> poc_ip_port_table;

    private ArrayList<int> visited_list;
    private ArrayList<int> optimal_path;

    public node(int PORT_NUM, String poc_name, int NUM_RINGO) {
        //Initialize Node
        this.PORT_NUM = PORT_NUM;
        this.poc_name = poc_name;
        this.NUM_RINGO = NUM_RINGO;

        //Create HashMaps for neighbors
        neighbor_map = new HashMap<int,long>();
        poc_to_ringo_num = new HashMap<String, int>();
        poc_ip_port_table = new HashMap<String, int>();

        //Create lists for routing
        visited_list = new ArrayList<>();
        optimal_path = new ArrayList<>();

        // Cost to self always 0
        add_neighbor_mapping(this.NUM_RINGO, 0);

        //Calcluate cost to PoC
        long cost = this.calculate_rtt(poc_name);

        if (cost > -1) {
            //TODO Get neigbor's Ringo number via a socket
            int poc_number = 0;
            add_poc_mapping(poc_number, cost);
        } else {
            System.out.println("Neighbor unreachable, left undiscovered");
        }

        //TODO Get neighbor's known Ringo map via a Socket
        //
    }

    public int get_Port_Num() {
        return this.PORT_NUM;
    }

    public String get_poc_name() {
        return this.poc_name;
    }

    public void add_neighbor_mapping(int ringo_number, long cost) {
        neighbor_map.put(ringo_number, cost);
    }

    public void add_poc_mapping(String ip_address, int poc_num) {
        poc_to_ringo_num.put(ip_address, poc_num);
        //PUNTED POCNUM DISCOVERY TO MAIN METHOD IN RINGO class
    }

    public HashMap<int,long> get_ring() {
        return neighbor_map;
    }

    public void print_ring() {
        //TODO: flesh out print statements to print out ring
    }

    private long calculate_rtt(String ip_address) {
        try {
            InetAddress inet = InetAddress.getByName(ip_address);
            long finish = 0;
            long start = new GregorianCalendar().getTimeInMillis();

            if (inet.isReachable(5000)){
                finish = new GregorianCalendar().getTimeInMillis();
                return (finish - start);
            } else {
                System.out.println(ip_address + " NOT reachable.");
                return -1
            }
        } catch ( Exception e ) {
            System.out.println("Exception:" + e.getMessage());
            return -1;
        }
    }
}





public class ringo {
    // globals needed for main method, other locals are stored in node class
    // node class responsible for all Ringo mappings and rtt calculations
    // this is the controller class

    // Hence, we have a model-view-controller architecture, where the view
    // is the mappings created by the model, and the model is the Ringo nodes
    // themselves. Since everything is modular, the code should be easier
    // to maintain.

    private static int NUM_ACTIVE_RINGO = 0;
    private static int PACKET_TRANSITION_NUMBER = 0;

    private static Queue<byte[]> IO_QUEUE = new ArrayDeque<>();
    private static Queue<String> PROCESS_QUEUE = new ArrayDeque<>();

    private static ArrayList<node> KNOWN_RINGO_LIST = new ArrayList<>();
    // status indicator
    private static String flag;

    public static void main(String[] args) {
        final ExecutorService receiver = Executors.newSingleThreadExecutor();
        final ExecutorService sender = Executors.newSingleThreadExecutor();

        while (true) {
            Scanner scanner = new Scanner(System.in);
            String[] in = scanner.nextLine().trim().split(" ");

            if (in.length == 6 && in[0].equals("ringo")) {
                // get the command
                flag = in[1];
                int ringo_portnumber = Integer.parseInt(in[2]);
                String poc_name = in[3];
                String poc_port_str = in[4];
                int poc_port = Integer.parseInt(poc_port_str);
                int ringo_id_num = Integer.parseInt(in[5]);

                //Create new node here
                node new_ringo = new node(ringo_portnumber, poc_name, ringo_id_num);

                receiver.submit(new Receiver(new_ringo.get_Port_Num()));

                //TODO: DO NEIGHBOR DISCOVERY, TELL NODE MODEL THE CHANGES

                //TODO: CALL NODE'S OPTIMAL RING METHOD AFTER DONE UPDATING MODEL

                //TODO: VERFIY OPTIMAL RING USING MAPPINGS IN NODE MODEL

                //TODO: ONCE VERIFIED OPTIMAL RING, SEND TO ALL OTHER RINGOS
                //      IN KNOWN_RINGO_LIST
            } else if (in.length == 2 && in[0].equals("offline")) {
                try {
                    Thread.sleep(Integer.parseInt(in[1]));
                } catch (InterruptedException e) {
                    System.out.println("node offline failed");
                }
            } else if (in.length == 2 && in[0].equals("send")) {
                //TODO: SET UP A SENDER
            } else if (in[0].equals("show-ring")) {
                //TODO: CHECK TO SEE IF NODE EXISTS USING TRY-CATCH
                //TODO: IF IT DOES, CALL THE print_ring METHOD
            } else if (in[0].equals("show-matrix")) {
                //TODO: CHECK TO SEE IF NODE EXISTS USING TRY-CATCH
                //TODO: IF IT DOES, CALL THE print_matrix METHOD
            } else if (in[0].equals("disconnect")) {
                //TODO: CHECK TO SEE IF NODE EXISTS USING TRY-catch
                //TODO: IF IT DOES, SEND OUT A DISCONNECT SIGNAL WITH THE
                //      RINGO ID NUMBER ATTACHED TO ALL OTHER RINGOS
                //TODO: ONCE MESSAGE RETURNS TO SPECIFIED NODE, DISCONNECT IT
            } else {
                System.out.println("invalid command, please try again !");
            }

            scanner.close();
        }
    }

    /**
     * the private sender class
     *
     * in charge of sending data from the IO_QUEUE
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


    /**
     * the receiver class
     *
     * in charge of receiving data and put it into IO_QUEUE
     */
    static class Receiver implements Runnable {
        int IN_PORT;

        public Receiver(int IN_PORT) {
            this.IN_PORT = IN_PORT;
        }

        public void run() {
            try {
                DatagramSocket socket = new DatagramSocket(IN_PORT);
                byte[] in_data = new byte[2500];
                while (true) {
                    DatagramPacket packet = new DatagramPacket(in_data, 2500);
                    socket.receive(packet);

                    //Check to see if there was data received
                    String temp = new String(packet.getData());
                    if (temp.substring(0, 9).equals("RTT Check")) {
                        PROCESS_QUEUE.add(temp);
                    } else if (temp.substring(0, 10).equals("keep alive")) {
                        PROCESS_QUEUE.add(temp);
                    } else {
                        IO_QUEUE.add(packet.getData());
                        PACKET_TRANSITION_NUMBER++;
                    }
                }
            } catch (SocketException e) {
                System.out.println("initializing socket failed");
            } catch (UnknownHostException e) {
                System.out.println("cannot solve the destination IP address");
            } catch (IOException e) {
                System.out.println("receiving data failed");
            }
        }
    }


    /**
     * add poc to known list if possible
     * @param poc_name the IP address or DNS name of poc
     * @param poc_port the port number of poc
     * THIS MAY BE DEPRECATED
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


    /**
     * send the rtt and known ringo list to a specific ringo
     * @param sender the multi-thread executive service
     * @param ip the target ip address
     * @param port the target port number
     */
    private static void send_ringo_rtt(ExecutorService sender, String ip, int port) {
        sender.submit(new Sender(ip, port));
        String temp = "RTT Check: " + KNOWN_RINGO_LIST.toString() + ":" + Arrays.deepToString(RTT);
        IO_QUEUE.add(temp.getBytes());
        sender.shutdown();
    }



    private static void update_rtt() {

    }


    /**
     * calculating the rtt between current ringo and specific ringo
     *
     * @param ip_address the target node ip address
     * @return the rtt
     */
    private static String calculate_rtt(String ip_address) {
        try {
            InetAddress inet = InetAddress.getByName(ip_address);
            long finish = 0;
            long start = new GregorianCalendar().getTimeInMillis();

            if (inet.isReachable(5000)){
                finish = new GregorianCalendar().getTimeInMillis();
                return Long.toString(finish - start);
            } else {
                System.out.println(ip_address + " NOT reachable.");
                return "-1";
            }
        } catch ( Exception e ) {
            System.out.println("Exception:" + e.getMessage());
            return "-1";
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
