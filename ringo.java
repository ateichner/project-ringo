import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.commons.lang.SerializationUtils;


/**
 * globals needed for main method, other locals are stored in node class
 * node class responsible for all Ringo mappings and rtt calculations
 * this is the controller class
 *
 * Hence, we have a model-view-controller architecture, where the view
 * is the mappings created by the model, and the model is the Ringo nodes
 * themselves. Since everything is modular, the code should be easier
 * to maintain.
 */
public class ringo {
    private static int NUM_ACTIVE_RINGO = 0;
    private static int PACKET_TRANSITION_NUMBER = 0;

    private static Queue<byte[]> IO_QUEUE = new ArrayDeque<>();
    private static Queue<byte[]> PROCESS_QUEUE = new ArrayDeque<>();

    private static boolean do_sending = false;

    private static List<node> KNOWN_RINGO_LIST = new ArrayList<>();
    private static String flag;

    private static final ExecutorService receiver_thread = Executors.newSingleThreadExecutor();
    private static final ExecutorService sender_thread = Executors.newSingleThreadExecutor();

    private static final String keep_alive_check = "message keep alive check";
    private static final String ringo_communication = "message ringo communication";

    public static void main(String[] args) {
        while (true) {
            Scanner scanner = new Scanner(System.in);
            String[] in = scanner.nextLine().trim().split(" ");

            if (in.length == 6 && in[0].equals("ringo")) {
                // digest the command
                flag = in[1];
                int self_ringo_port = Integer.parseInt(in[2]);
                String poc_name = in[3];
                String poc_port_str = in[4];
                int poc_port = Integer.parseInt(poc_port_str);
                int ringo_id_num = Integer.parseInt(in[5]);

                // start the receiver_thread
                receiver_thread.submit(new Receiver(poc_port));

                //Create new node here
                node poc_node = new node(self_ringo_port, poc_name, ringo_id_num);
                KNOWN_RINGO_LIST.add(poc_node);

                // do neighbor discovery, tell node model the changes
                sender_thread.submit(new Sender(poc_name, poc_port));
                IO_QUEUE.add((ringo_communication + ":::" + KNOWN_RINGO_LIST.toString()).getBytes());
                do_sending = true;
                

                // TODO: CALL NODE'S OPTIMAL RING METHOD AFTER DONE UPDATING MODEL

                // TODO: VERIFY OPTIMAL RING USING MAPPINGS IN NODE MODEL

                // TODO: ONCE VERIFIED OPTIMAL RING, SEND TO ALL OTHER RINGOS
                //      IN KNOWN_RINGO_LIST
            } else if (in.length == 2 && in[0].equals("offline")) {
                try {
                    Thread.sleep(Integer.parseInt(in[1]));
                } catch (InterruptedException e) {
                    System.out.println("node offline failed");
                }
            } else if (in.length == 2 && in[0].equals("send")) {
                // TODO:
                // setup a sender_thread with the out_ip and out_port of next node in the optimal ring

            } else if (in[0].equals("show-ring")) {
                // TODO: CHECK TO SEE IF NODE EXISTS USING TRY-CATCH

                // TODO: IF IT DOES, CALL THE print_ring METHOD
            } else if (in[0].equals("show-matrix")) {
                // TODO: CHECK TO SEE IF NODE EXISTS USING TRY-CATCH

                // TODO: IF IT DOES, CALL THE print_matrix METHOD
            } else if (in[0].equals("disconnect")) {
                // TODO: CHECK TO SEE IF NODE EXISTS USING TRY-catch

                // TODO: IF IT DOES, SEND OUT A DISCONNECT SIGNAL WITH THE RINGO ID NUMBER ATTACHED TO ALL OTHER RINGOS

                // TODO: ONCE MESSAGE RETURNS TO SPECIFIED NODE, DISCONNECT IT

            } else {
                System.out.println("invalid command, please try again !");
            }

            scanner.close();
        }
    }

    /**
     * the private sender_thread class
     *
     * in charge of sending data from the IO_QUEUE
     */
    static class Sender implements Runnable{
        String OUT_IP;
        int OUT_PORT;

        /**
         * the constructor of sender
         *
         * @param out_ip the target ip address
         * @param out_port the target port number
         */
        Sender(String out_ip, int out_port) {
            this.OUT_IP = out_ip;
            this.OUT_PORT = out_port;
        }

        /**
         * the default multi-thread method
         */
        public void run() {
            send();
        }

        /**
         * sending method
         */
        private void send() {
            try {
                DatagramSocket socket = new DatagramSocket();

                // get ip destination wanted
                InetAddress IPF = InetAddress.getByName(OUT_IP);

                // send data
                while (true) {
                    if (IO_QUEUE.size() != 0 && do_sending) {
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
     * the receiver_thread class
     *
     * in charge of receiving data and put it into IO_QUEUE
     */
    static class Receiver implements Runnable {
        int IN_PORT;

        /**
         * the constructor of sender
         *
         * @param IN_PORT the incoming port number
         */
        Receiver(int IN_PORT) {
            this.IN_PORT = IN_PORT;
        }

        /**
         * the default multi-thread method
         */
        public void run() {
            receive();
        }

        /**
         * receiving method
         */
        private void receive() {
            try {
                DatagramSocket socket = new DatagramSocket(IN_PORT);
                byte[] in_data = new byte[2500];
                while (true) {
                    DatagramPacket packet = new DatagramPacket(in_data, 2500);
                    socket.receive(packet);

                    //Check to see if there was data received
                    message_check(packet.getData());

                }
            } catch (SocketException e) {
                System.out.println("initializing socket failed");
            } catch (UnknownHostException e) {
                System.out.println("cannot solve the destination IP address");
            } catch (IOException e) {
                System.out.println("receiving data failed");
            }
        }

        /**
         * check the incoming message for further actions
         * @param data the incoming byte array
         */
        private void message_check(byte[] data) {

            try {
                PROCESS_QUEUE.add(data);

            } catch (Exception e) {
                System.out.println("error");
            }

//            if (string_from_byte_array.contains(ringo_communication)) {
//                // do the ringo data exchange
//                PROCESS_QUEUE.add(string_from_byte_array);
//
//            } else if (string_from_byte_array.contains(keep_alive_check)) {
//                PROCESS_QUEUE.add(string_from_byte_array);
//
//            } else {
//                IO_QUEUE.add(data);
//                PACKET_TRANSITION_NUMBER++;
//            }
        }
    }

    static class Processer implements Runnable {
        public void run() {

        }

        private void process() {
            while (true) {
                if (PROCESS_QUEUE.size() != 0) {
                    List<node> received_known_list = fromByteArrayToJava(PROCESS_QUEUE.poll());
                    process_ringo_communication(received_known_list);
                }
            }
        }

        private void process_ringo_communication(List<node> received_known_list) {
            // TODO: process the received data
        }

        private void process_keep_alive() {

        }
    }


    /**
     * Convert object to byte array
     * @param object the node object will be converted
     * @return te converted byte array
     */
    public static byte[] fromJavaToByteArray(Serializable object) {
        return SerializationUtils.serialize(object);
    }


    /**
     * Convert byte array to object
     * @param bytes the byte array will be converted
     * @return the converted node
     */
    public static ArrayList<node> fromByteArrayToJava(byte[] bytes) {
        return (ArrayList<node>) SerializationUtils.deserialize(bytes);
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



//    previous code
//
//    might be deprecated


//    /**
//     * add poc to known list if possible
//     * @param poc_name the IP address or DNS name of poc
//     * @param poc_port the port number of poc
//     * THIS MAY BE DEPRECATED
//     */
//    private static void add_poc(String poc_name, String poc_port) {
//        for (int i = 0; i < KNOWN_RINGO_LIST.size(); i++) {
//            if (KNOWN_RINGO_LIST.get(i)[0].equals(poc_name)) {
//                System.out.println(poc_name + " is already in known_list");
//                break;
//            } else {
//
//                KNOWN_RINGO_LIST.add(new String[] {poc_name, poc_port});
//            }
//        }
//    }


//    /**
//     * send the rtt and known ringo list to a specific ringo
//     * @param sender_thread the multi-thread executive service
//     * @param ip the target ip address
//     * @param port the target port number
//     */
//    private static void send_ringo_rtt(ExecutorService sender_thread, String ip, int port) {
//        sender_thread.submit(new Sender(ip, port));
//        String temp = "RTT Check: " + KNOWN_RINGO_LIST.toString() + ":" + Arrays.deepToString(RTT);
//        IO_QUEUE.add(temp.getBytes());
//        sender_thread.shutdown();
//    }
}