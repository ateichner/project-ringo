import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class Ringo {
    /**
     * the private sender_thread class
     *
     * in charge of sending data from the MessageQueue
     */
    static class MessageSender implements Runnable{
        Message message;

        MessageSender(Message message) {
            this.message = message;
        }

        /**
         * the default multi-thread method
         */
        public void run() {
            System.out.println("message sender is active");
            send();
        }


        /**
         * sending method
         */
        private void send() {
            try {
                DatagramSocket socket = new DatagramSocket();

                // send data

                String outData = this.message.getOutData();
                byte[] out_data = outData.getBytes();

                for (Node n : this.message.getDestinations()) {
                    DatagramPacket sendPkt = new DatagramPacket(out_data, out_data.length, InetAddress.getByName(n.getIp()), n.getPort());
                    System.out.println("Message transmitted:  " + "ip: " + InetAddress.getByName(n.getIp()).toString() + " | " + "port: " + n.getPort());
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


    /**
     * the receiver_thread class
     *
     * in charge of receiving data and put it into IO_QUEUE
     */
    static class MessageReceiver implements Runnable {
        static int IN_PORT;
        static DatagramSocket socket;


        /**
         * the constructor of sender
         *
         * @param IN_PORT the incoming port number
         */
        MessageReceiver(int IN_PORT) {
            this.IN_PORT = IN_PORT;
        }


        /**
         * the default multi-thread method
         */
        public void run() {
            System.out.println("message receiver is active at port " + IN_PORT);
            receive();
        }


        /**
         * receiving method
         */
        private void receive() {
            setConnection();

            while (true) {
                byte[] in_data = new byte[2500];
                try {
                    DatagramPacket packet = new DatagramPacket(in_data, in_data.length);
                    socket.receive(packet);
                    System.out.println("get packet");

                    //Check to see if there was data received
                    process(packet.getData());
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
         * check the incoming message for further actions
         * @param data the incoming byte array
         */
        private void process(byte[] data) {
            System.out.println("processing packet");

            String in = new String(data).trim();

            Message m = new Message(in);

        }


        private static void setConnection() {
            try {
                // create the listener of this server. ServerSocket will automatically create a socket and bind it to the port.
                // second parameter 50 means the back_log number, maximum 50 in the queue.
                socket = new DatagramSocket(IN_PORT);
                System.out.println("Server is now running at port: " + IN_PORT);
            } catch (IOException e) {
                System.out.println("an IOException found, listening socket setup failed");
            }
        }
    }


    // globals
    private static final ExecutorService receiver_thread = Executors.newSingleThreadExecutor();
    private static final ExecutorService sender_thread = Executors.newSingleThreadExecutor();

    private static Node selfNode;
    private static int NUM_RINGO = 0;
    private static int NUM_ACTIVE_RINGO = 0;
    private static int PACKET_TRANSMISSION_NUMBER = 0;

    private static Queue<byte[]> IO_QUEUE = new ArrayDeque<>();

    // Data structure mapping this nodes neighbors to the costs of getting to each neighbor.
    private static HashMap<Node, Float> costToNeighborMap = new HashMap<>();

    // Data structure to hold the most recently received message from each neighbor
    private static HashMap<Node, Message> messages = new HashMap<>();
    private static boolean newMessages = false;
    private static Queue<Message> messageQueue = new ArrayDeque<>();

    // HashMap distanceVector holds the cost to each destination from this selfNode
    private static HashMap<Node, Float> distanceVector = new HashMap<>();

    // HashMap forwardingTable holds the next hop to each destination from this selfNode
    private static HashMap<Node, Node> forwardingTable = new HashMap<>();

    private static String flag;

    private static final String keep_alive_check = "message keep alive check";

    /**
     * Main method
     * @param args some arguments
     */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String[] in = scanner.nextLine().trim().split(" ");

            if (in.length == 6 && in[0].equals("ringo")) {
                // digest the command
                flag = in[1];
                int selfPort = Integer.parseInt(in[2]);
                String pocName = in[3];
                String pocPortStr = in[4];
                int pocPort = Integer.parseInt(pocPortStr);
                int numRingo = Integer.parseInt(in[5]);

                // set up self selfNode
                selfNode = new Node(getSelfIP(), selfPort);
                Float selfCost = (0.0f);
                addNeighbor(selfNode, selfCost);
                System.out.println("Started the selfNode");

                // start the receiver_thread
                receiver_thread.submit(new MessageReceiver(selfPort));

                // if this ringo has a PoC
                boolean hasPoC = !pocName.equals("0") & pocPort != 0;
                if (hasPoC) {
                    // Create new selfNode here
                    if (getIP(pocName) == null) {
                        System.out.println("unknown poc name");
                        continue;
                    }

                    // code
                    addNeighbor(new Node(getIP(pocName), pocPort), calculate_rtt(pocName));
                    System.out.println("Started PoC");
                    doDistanceVectorUpdate();
                    System.out.println("Distance vector updated");
                }

                // TODO: CALL NODE'S OPTIMAL RING METHOD AFTER DONE UPDATING MODEL

                // TODO: VERIFY OPTIMAL RING USING MAPPINGS IN NODE MODEL

                // TODO: ONCE VERIFIED OPTIMAL RING, SEND TO ALL OTHER RINGOS
                //      IN KNOWN_RINGO_LIST
            } else if (in.length == 2 && in[0].equals("offline")) {
                try {
                    Thread.sleep(Integer.parseInt(in[1]));
                } catch (InterruptedException e) {
                    System.out.println("selfNode offline failed");
                }
            } else if (in.length == 2 && in[0].equals("send")) {
                // TODO: setup a sender_thread with the out_ip and out_port of next selfNode in the optimal ring
                System.out.println("send test");


            } else if (in[0].equals("show-ring")) {
                // TODO: CHECK TO SEE IF NODE EXISTS USING TRY-CATCH

                // TODO: IF IT DOES, CALL THE print_ring METHOD
                System.out.println("show-ring test");
                printDistanceVector();

            } else if (in[0].equals("show-matrix")) {
                // TODO: CHECK TO SEE IF NODE EXISTS USING TRY-CATCH

                // TODO: IF IT DOES, CALL THE print_matrix METHOD
                System.out.println("show-matrix test");

            } else if (in[0].equals("disconnect")) {
                // TODO: CHECK TO SEE IF NODE EXISTS USING TRY-catch

                // TODO: IF IT DOES, SEND OUT A DISCONNECT SIGNAL WITH THE RINGO ID NUMBER ATTACHED TO ALL OTHER RINGOS

                // TODO: ONCE MESSAGE RETURNS TO SPECIFIED NODE, DISCONNECT IT
                System.out.println("disconnect test");

            } else {
                System.out.println("invalid command, please try again !");
            }
        }
    }


    /**
     * change the cost to one neighbor
     * @param neighbor the neighbor will be changed
     * @param cost the new cost will be updated
     * @throws Exception some errors
     */
    public static void changeCostToNeighbor(Node neighbor, float cost) throws Exception {
        if (!costToNeighborMap.containsKey(neighbor)) {
            throw new Exception("Trying to change cost to a node that isn't already a neighbor");
        }

        // Change the cost to this neighbor
        costToNeighborMap.put(neighbor, cost);

        // Update the local routing info (distance vector and forwarding table)
        // with the new cost
        doDistanceVectorUpdate();
        clearNewMessagesFlag();

        // Notify neighbors of the change
        notifyNeighbors();
    }


    /**
     * update the forwarding table with given information
     * @param destination the final destination
     * @param nextHop next hop in the path to the final destination
     * @throws Exception error while updating
     */
    private static void updateForwardingTable(Node destination, Node nextHop) throws Exception {
        if ((!costToNeighborMap.containsKey(nextHop)) && (nextHop != selfNode)) {
            throw new Exception("Trying to add a forwarding table entry to a node that isn't a neighbor");
        }
        forwardingTable.put(destination, nextHop);
    }



    /**
     * Implements the Bellman-Ford equation to update the distanceVector costs
     * and forwardingTable of this node
     */
    private static void doDistanceVectorUpdate() {
        // STEP 1: Fill in this method

        ArrayList<Node> nextNodes = new ArrayList<>();
        ArrayList<Float> costs = new ArrayList<>();
        boolean somethingChanged = false;

        // Loops over all possible destinations.
        for (Node destination : getDestinations()) {

            // Loops over all neighbors.
            // Do Bellman-Ford updates using this node's local info
            for (Node neighbor : getNeighbors()) {

                // Reinitialize the node.
                if (selfNode.equals(destination)) {

                    nextNodes.add(selfNode);
                    costs.add(0.0f);

                } else {

                    Float cost = getCostToNeighbor(neighbor);

                    if ((messages.containsKey(destination)))
                        cost += getCostFromNeighborTo(neighbor,
                                destination);
                    else
                        cost += getCostToDestination(destination);

                    nextNodes.add(neighbor);
                    costs.add(cost);
                }
            }

            Float minimumDistance = Float.POSITIVE_INFINITY;
            Node nextHop = null;

            // Gets minimum distance and Hop.
            for (int i = 0; i < costs.size() && i < nextNodes.size(); i++) {

                if (costs.get(i) < minimumDistance) {

                    minimumDistance = costs.get(i);
                    nextHop = nextNodes.get(i);
                }
            }

            try {
                // Updates the distance if different from stored value.
                if (minimumDistance != getCostToDestination(destination)) {

                    updateDistanceVector(destination, minimumDistance);
                    System.out.println("Destination: " + destination);
                    System.out.println("nextHop: " + nextHop);
                    if (nextHop != null) {
                        updateForwardingTable(destination, nextHop);
                    }
                    somethingChanged = true;

                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            nextNodes.clear();
            costs.clear();
        }

        // If something changed, notifies this node's neighbors.
        if (somethingChanged) {
            somethingChanged = false;
            notifyNeighbors();
        }
    }


    /**
     * Check if this Node has received a new distance vector message from one of
     * its neighbors
     *
     * @return true if it has received a new message, false otherwise
     */
    public static boolean hasNewMessages() {
        return newMessages;
    }

    /**
     * Set this node's newMessages flag to false
     */
    public static void clearNewMessagesFlag() {
        newMessages = false;
    }


    /**
     * Send a distance vector message to this node. Adds the message to this
     * node's message queue.
     *
     * @param m the message
     */
    public static void sendMessage(Message m) {
        sender_thread.submit(new MessageSender(m));
    }


    /**
     * Gets the messages from the MessageQueue and puts it into a HashMap
     */
    public static void deliverMessageQueue() {
        for (Message m: messageQueue) {
            messages.put(m.getFrom(), m);
        }
        messageQueue.clear();
        newMessages = true;
    }


    /**
     * Method used by the Network to tell this selfNode that the list of possible
     * destinations has changed.
     *
     * @param destinations other nodes
     */
    public static void updateDestinations(Collection<Node> destinations) {
        for (Node n : destinations) {
            // Only add if don't already know about n
            if (!distanceVector.containsKey(n)) {
                distanceVector.put(n, Float.POSITIVE_INFINITY);
                forwardingTable.put(n, null);
            }
        }
    }

    /**
     * Add a new neighbor to this selfNode and specifies the cost to this neighbor
     *
     * @param neighbor is the neighboring Node
     * @param cost is the non-negative integer cost to get to this neighbor
     */
    private static void addNeighbor(Node neighbor, float cost) {
        if ((costToNeighborMap.containsKey(neighbor)) || (cost < 0)) {
            String message = "Error adding neighbor to selfNode" + selfNode + "("
                    + neighbor + ", " + cost + ")"
                    + "\nCan't have duplicate links or negative costs";
            System.out.println(message);
        }

        // Add an entry for the new neighbor in the local data structures
        costToNeighborMap.put(neighbor, cost);
        messages.put(neighbor, null);
        if (!distanceVector.containsKey(neighbor)) {
            distanceVector.put(neighbor, cost);
            forwardingTable.put(neighbor, neighbor);
        }

        // Send a message to all neighbors with this new cost info
        notifyNeighbors();
    }


    /**
     * Get a collection of all destinations in the network from this selfNode
     *
     * @return a Collection of all possible destinations from this selfNode
     */
    private static Collection<Node> getDestinations() {
        return new TreeSet<>(distanceVector.keySet());
    }


    /**
     * @return a Collection of this selfNode's neighbors
     */
    private static Collection<Node> getNeighbors() {
        return new TreeSet<>(costToNeighborMap.keySet());
    }


    /**
     * Get the cost of the link to go directly from this selfNode to a neighbor
     *
     * @param neighbor
     *            is a selfNode directly connected to this one
     * @return the link cost to go directly to this neighbor, or infinity if the
     *         specified selfNode isn't a neighbor
     */
    private static float getCostToNeighbor(Node neighbor) {
        return costToNeighborMap.getOrDefault(neighbor, Float.POSITIVE_INFINITY);
    }

    /**
     * Get the cost from a neighbor to a destination, as advertised in the
     * latest message received from that neighbor
     *
     * @param neighbor is the potential next hop
     * @param destination a potential selfNode we are trying to get to
     * @return the cost from neighbor to destination as advertised in the most
     *         recent message received from neighbor
     */
    private static float getCostFromNeighborTo(Node neighbor, Node destination) {
        Message m = messages.get(neighbor);
        if (m != null) {
            return m.getCostTo(destination);
        } else {
            return Float.POSITIVE_INFINITY;
        }
    }


    /**
     * Get the next hop listed in this nodes forwardingTable to get to a
     * specified destination
     *
     * @param destination is another Node in the Network
     * @return the Node that is the next hop to get from this selfNode to the
     *         specified destination
     */
    private static Node getNextHopTo(Node destination) {
        return forwardingTable.get(destination);
    }


    /**
     * Get the current distanceVector entry from this selfNode to a specified
     * destination
     *
     * @param destination is another Node in the Network
     * @return the cost to get from this Node to destination in our current
     *         distanceVector
     */
    private static float getCostToDestination(Node destination) {
        return distanceVector.get(destination);
    }


    /**
     * Updates the Distance Vector
     *
     * @param destination the final node
     * @param cost
     * @throws Exception
     */
    private static void updateDistanceVector(Node destination, float cost)
            throws Exception {
        if (cost > 0) {
            distanceVector.put(destination, cost);
        } else {
            throw new Exception("Costs can't be negative");
        }
    }


    /**
     * Sends distance vector messages to all neighbors of this selfNode
     */
    private static void notifyNeighbors() {
        // STEP 2: Fill in this method

        HashMap<Node, Float> vector = new HashMap<>();

        // Gets the selfNode's distance vector.
        for (Node destination : getDestinations()) {
            vector.put(destination, getCostToDestination(destination));
        }
        // (Not doing poisoned reverse in this implementation)

        // Compiles the selfNode's distance vector.
        Message message = new Message(selfNode, vector, getNeighbors());

        // Send the message to every neighbor.
        sendMessage(message);
    }


    /**
     * print the latest Message
     *
     * can be converted to show_matrix or show_ringo
     */
    private static void printLatestMessages() {
        System.out.println("Latest messages received by node " + selfNode + ":");
        System.out.println("      \t| Neighbors");
        System.out.print("Dest. \t|");
        Collection<Node> neighbors = getNeighbors();
        for (Node n : neighbors) {
            System.out.print("\t" + n);
        }
        System.out.print("\n");
        System.out.print("---------------");
        for (int i = 0; i < neighbors.size(); i++) {
            System.out.print("--------");
        }
        System.out.print("\n");
        for (Node dest : getDestinations()) {
            System.out.print(dest + "\t|");
            for (Node n : neighbors) {
                String costFromNeighborTo = "";
                if (getCostFromNeighborTo(n, dest) == Float.POSITIVE_INFINITY) {
                    costFromNeighborTo = "Inf";
                } else {
                    // costFromNeighborTo =
                    // Float.toString(getCostFromNeighborTo(n,dest));
                    costFromNeighborTo = Integer
                            .toString((int) getCostFromNeighborTo(n, dest));
                }
                System.out.print("\t" + costFromNeighborTo);
            }
            System.out.print("\n");
        }
        System.out.println(" ");
    }


    /**
     * print the distance vector
     */
    private static void printDistanceVector() {
        System.out.println("Distance vector and forwarding table for node "
                + selfNode + ":");
        System.out.println("Dest.\tCost (Next Hop)");
        System.out.println("-------------------------");
        for (Node dest : getDestinations()) {
            String costToDestination = "";
            if (getCostToDestination(dest) == Float.POSITIVE_INFINITY) {
                costToDestination = "Inf";
            } else {
                costToDestination = Integer
                        .toString((int) getCostToDestination(dest));
            }
            System.out.println(dest + "\t" + costToDestination + " ("
                    + getNextHopTo(dest) + ")");
        }
        System.out.println("");
    }


    /**
     * get the current ringo's ip address
     *
     * @return the current ringo's ip address
     */
    private static String getSelfIP() {
        try {
            URL url_name = new URL("http://bot.whatismyipaddress.com");
            BufferedReader sc = new BufferedReader(new InputStreamReader(url_name.openStream()));
            return sc.readLine().trim();
        }
        catch (Exception e) {
            return null;
        }
    }

    /**
     * getIP gets an IP address from a specified name
     *
     * @param input the hostname
     * @return the IPAddress
     */
    private static String getIP(String input) {
        try {
            return InetAddress.getByName(input).getHostAddress();
        } catch (UnknownHostException e) {
            return null;
        }
    }


    /**
     * calculating the rtt between current ringo and specific ringo
     *
     * @param ip_address the target node ip address
     * @return the rtt
     */
    private static float calculate_rtt(String ip_address) {
        float start = -1;
        float rtt = -1;
        float stop = -1;
        //Create IP UDP connection
        try {
            InetAddress ip = InetAddress.getByName(ip_address);
            boolean reachale = ip.isReachable(160000);

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
