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
            System.out.println('\n');
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

                String dataToBeSent = this.message.getdataToBeSent();
                byte[] data_to_be_sent = dataToBeSent.getBytes();

                for (Node n : this.message.fetchDestinations()) {
                    if (n.equals(ringoNode)) {
                        continue;
                    }
                    DatagramPacket sendPkt = new DatagramPacket(data_to_be_sent, data_to_be_sent.length, InetAddress.getByName(n.getIp()), n.getPort());
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
     * the sender in charge of sending strings
     */
    static class StringSender implements Runnable{
        String data;
        String ip;
        int port;

        StringSender(String data, String ip, int port) {
            this.data = data;
            this.ip = ip;
            this.port = port;
        }

        /**
         * the default multi-thread method
         */
        public void run() {
            System.out.println('\n');
            System.out.println("string sender is active");
            send();
        }


        /**
         * sending method
         */
        private void send() {
            try {
                DatagramSocket socket = new DatagramSocket();

                // send data
                byte[] data_to_be_sent = data.getBytes();
                DatagramPacket sendPkt = new DatagramPacket(data_to_be_sent, data_to_be_sent.length, InetAddress.getByName(ip), port);
                System.out.println("String transmitted:  " + "ip: " + ip + " | " + "port: " + port);
                socket.send(sendPkt);

            } catch (SocketException e) {
                System.out.println("initializing socket failed");
            } catch (UnknownHostException e) {
                System.out.println("cannot solve the destination IP addresssss");
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
        int IN_PORT;
        DatagramSocket socket;


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
                byte[] in_data = new byte[5000];
                try {
                    DatagramPacket packet = new DatagramPacket(in_data, in_data.length);
                    socket.receive(packet);

                    String inIP = packet.getAddress().toString();
                    int inPort = packet.getPort();
                    System.out.println('\n');
                    System.out.println("get packet from " + inIP + "|" + inPort);

                    //Check to see if there was data received
                    process(packet.getData(), inIP, inPort);
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
        private void process(byte[] data, String inIP, int inPort) {
            System.out.println("processing packet");

            String in = new String(data).trim();

            if (in.contains("ping:")) {
                System.out.println("ping request from: " + in.split(":")[1] + in.split(":")[2]);
                sender_thread.submit(new StringSender("ping-ack", in.split(":")[1], Integer.parseInt(in.split(":")[2])));
            } else if (in.equals("ping-ack")) {
                System.out.println("ping acked");
                pingAck = System.currentTimeMillis();
            } else {
                Message m = new Message(in);
                System.out.println("M in process method: "+in);
                deliverMessage(m);
                if (m.getFrom() != null) {
                    System.out.println("Attempting to update DV");
                    try {

                        System.out.println("m.fetchDestinations object ref: " + (m.fetchDestinations().toString()));
                    } catch (Exception e) {
                        System.out.println(e + ": m.fetchDestinations() is null in process method");
                    }
                    boolean printed = false;
                    for (Node n: m.fetchDestinations()) {
                        // if (!distanceVector.containsKey(n)) {
                        if (!printed) {
                            System.out.println("IN FOREACH INSIDE process METHOD");
                            printed = true;
                        }
                        if (n != null) {
                            System.out.println("Node n: "+n.toString());
                        } else {
                            System.out.println("Node n is null in FOREACH INSIDE process METHOD");
                        }

                        addNeighbor(n, calculate_rtt(n.getIp(), n.getPort()), m);
                        // messages.put(n, m);
                        // }
                    }
                    calculateDistanceVector();
                }




                //}
                //if ((m.getFrom()))
                //changeSingleNeighborCost(m.)

            }


        }


        private void setConnection() {
            try {
                socket = new DatagramSocket(IN_PORT);
            } catch (IOException e) {
                System.out.println("an IOException found, listening socket setup failed");
            }
        }
    }


    // globals
    private static final ExecutorService receiver_thread = Executors.newSingleThreadExecutor();
    private static final ExecutorService sender_thread = Executors.newCachedThreadPool();

    private static Node ringoNode;
    private static int NUM_RINGO = 0;
    private static int NUM_ACTIVE_RINGO = 0;
    private static int PACKET_TRANSMISSION_NUMBER = 0;

    private static Queue<byte[]> IO_QUEUE = new ArrayDeque<>();

    // Data structure mapping this nodes neighbors to the costs of getting to each neighbor.
    private static HashMap<Node, Float> neighborsCosts = new HashMap<>();

    // Data structure to hold the most recently received message from each neighbor
    private static HashMap<Node, Message> messages = new HashMap<>();
    private static boolean newMessages = false;
    private static Queue<Message> messageQueue = new ArrayDeque<>();

    // HashMap distanceVector holds the cost to each destination from this ringoNode
    private static HashMap<Node, Float> distanceVector = new HashMap<>();

    // HashMap forwardingTable holds the next hop to each destination from this ringoNode
    private static HashMap<Node, Node> forwardingTable = new HashMap<>();

    private static String flag;
    private static float pingAck = (float) System.currentTimeMillis();

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
                boolean hasPoC = !pocName.equals("0") & pocPort != 0;

                // set up self ringoNode
                ringoNode = new Node(getMyIP(), selfPort);
                HashMap<Node, Float> costMap = new HashMap<>();
                costMap.put(ringoNode, 0.0f);


                Message aMessage = new Message(ringoNode, costMap, fetchDestinations());
                addNeighbor(ringoNode, 0.0f, aMessage);
                System.out.println("Started the ringoNode");

                // start the receiver_thread
                receiver_thread.submit(new MessageReceiver(selfPort));

                // if this ringo has a PoC
                if (hasPoC) {
                    // Create new ringoNode here
                    if (getIP(pocName) == null) {
                        System.out.println("unknown poc name");
                        continue;
                    }

                    // code
                    pocName = getIP(pocName);
                    float pocRTT = calculate_rtt(pocName, pocPort);
                    Node pocNode = new Node(pocName, pocPort);
                    HashMap<Node, Float> pocCostMap = new HashMap<>();
                    pocCostMap.put(ringoNode, pocRTT);
                    pocCostMap.put(pocNode, 0.0f);

                    Message pocMessage = new Message(pocNode, pocCostMap, fetchDestinations());
                    addNeighbor(pocNode, pocRTT, pocMessage);

                    System.out.println("Started PoC: " + pocName + "|" + pocPort);
                    calculateDistanceVector();
                    System.out.println("Distance vector updated");
                    printVectorMatrix();
                }

                // TODO: CALL NODE'S OPTIMAL RING METHOD AFTER DONE UPDATING MODEL

                // TODO: VERIFY OPTIMAL RING USING MAPPINGS IN NODE MODEL

                // TODO: ONCE VERIFIED OPTIMAL RING, SEND TO ALL OTHER RINGOS
                //      IN KNOWN_RINGO_LIST
            } else if (in.length == 2 && in[0].equals("offline")) {
                try {
                    Thread.sleep(Integer.parseInt(in[1]));
                } catch (InterruptedException e) {
                    System.out.println("ringoNode offline failed");
                }
            } else if (in.length == 2 && in[0].equals("send")) {
                // TODO: setup a sender_thread with the out_ip and out_port of next ringoNode in the optimal ring
                System.out.println("send test");


            } else if (in[0].equals("show-ring")) {
                // TODO: CHECK TO SEE IF NODE EXISTS USING TRY-CATCH

                // TODO: IF IT DOES, CALL THE print_ring METHOD
                System.out.println("show-ring test");
                printLatestMessages();

            } else if (in[0].equals("show-matrix")) {
                printVectorMatrix();

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
    public static void changeSingleNeighborCost(Node neighbor, float cost) throws Exception {
        if (!neighborsCosts.containsKey(neighbor)) {
            throw new Exception("Trying to change cost to a node that isn't already a neighbor");
        }

        // Change the cost to this neighbor
        neighborsCosts.put(neighbor, cost);

        // Update the local routing info (distance vector and forwarding table)
        // with the new cost

        calculateDistanceVector();
        clearNewMessagesFlag();

        // Notify neighbors of the change
        alertRingos();
    }


    /**
     * update the forwarding table with given information
     * @param destination the final destination
     * @param nextHop next hop in the path to the final destination
     * @throws Exception error while updating
     */
    private static void updateForwardingTable(Node destination, Node nextHop) throws Exception {
        if ((!neighborsCosts.containsKey(nextHop)) && (nextHop != ringoNode)) {
            throw new Exception("Trying to add a forwarding table entry to a node that isn't a neighbor");
        }
        forwardingTable.put(destination, nextHop);
    }



    /**
     * Implements the Bellman-Ford equation to update the distanceVector costs
     * and forwardingTable of this node
     */
    private static void calculateDistanceVector() {
        // STEP 1: Fill in this method
        System.out.println("INSIDE DISTANCE VECTOR UPDATE");
        ArrayList<Node> nextNodes = new ArrayList<>();
        ArrayList<Float> costs = new ArrayList<>();
        boolean somethingChanged = false;

        // Loops over all possible destinations.
        for (Node destination : fetchDestinations()) {

            // Loops over all neighbors.
            // Do Bellman-Ford updates using this node's local info
            for (Node neighbor : getAllKnownNeighbors()) {

                // Reinitialize the node.
                if (ringoNode.equals(destination)) {

                    nextNodes.add(ringoNode);
                    costs.add(0.0f);

                } else {

                    Float cost = getCostToSingleNeighbor(neighbor);
                    System.out.println("Cost to neighbor " + neighbor.toString() + ": " + cost);
                    boolean destInMessages = messages.containsKey(destination);
                    boolean neighborInMessages = messages.containsKey(neighbor);
                    System.out.println("Does messages contain " + destination.toString() + ": " + destInMessages);
                    System.out.println("Does messages contain " + neighbor.toString() + ": " + neighborInMessages);
                    if (destInMessages) {
                        Message m = messages.get(neighbor);
                        cost += getCostFromOneArbitraryNeighborToAnother(neighbor,
                                destination);
                        System.out.println("New Cost: " + cost);
                    } else {
                        cost += findCostToDestination(destination);
                        System.out.println("New Cost: " + cost);
                    }
                    nextNodes.add(neighbor);
                    // System.out.println("NEXTNODES LIST: " + nextNodes.toArray(Node[] nodes).toString());
                    costs.add(cost);
                    // System.out.println("COSTS LIST: " + costs.toArray(Float[] costs).toString());
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
                if (minimumDistance != findCostToDestination(destination)) {

                    updateDistanceVector(destination, minimumDistance);
                    System.out.println("Destination: " + destination.toString());
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
            alertRingos();
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
    public static void deliverMessage(Message m) {
        System.out.println("deliver the message");
        try {
            System.out.println("m.getFrom() in deliverMessage: "+(m.getFrom()).toString());
        } catch (Exception e) {
            System.out.print(e + ": m.getFrom() returned null in deliverMessage");
        }
        messages.put(m.getFrom(), m);
        newMessages = true;
    }


    /**
     * Method used by the Network to tell this ringoNode that the list of possible
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
     * Add a new neighbor to this ringoNode and specifies the cost to this neighbor
     *
     * @param neighbor is the neighboring Node
     * @param cost is the non-negative integer cost to get to this neighbor
     */
    private static void addNeighbor(Node neighbor, float cost) {
        System.out.println("IN ADD NEIGHBOR METHOD");
        if ((neighborsCosts.containsKey(neighbor)) || (cost < 0)) {
            String message = "Error adding neighbor to ringoNode" + ringoNode + "("
                    + neighbor + ", " + cost + ")"
                    + "\nCan't have duplicate links or negative costs";
            System.out.println("Message in addNeighbor: "+message);
        }

        // Add an entry for the new neighbor in the local data structures
        neighborsCosts.put(neighbor, cost);
        messages.put(neighbor, null);
        if (!distanceVector.containsKey(neighbor)) {
            distanceVector.put(neighbor, cost);
            forwardingTable.put(neighbor, neighbor);
        }

        // Send a message to all neighbors with this new cost info
        alertRingos();
    }

    private static void addNeighbor(Node neighbor, float cost, Message aMessage) {
        System.out.println("IN ADD NEIGHBOR METHOD");
        if ((neighborsCosts.containsKey(neighbor)) || (cost < 0)) {
            String message = "Error adding neighbor to ringoNode" + ringoNode + "("
                    + neighbor + ", " + cost + ")"
                    + "\nCan't have duplicate links or negative costs";
            System.out.println("Message in addNeighbor: "+message);
        }

        // Add an entry for the new neighbor in the local data structures
        neighborsCosts.put(neighbor, cost);
        messages.put(neighbor, aMessage);
        if (!distanceVector.containsKey(neighbor)) {
            distanceVector.put(neighbor, cost);
            forwardingTable.put(neighbor, neighbor);
        }

        // Send a message to all neighbors with this new cost info
        alertRingos();
    }

    /**
     * Get a collection of all destinations in the network from this ringoNode
     *
     * @return a Collection of all possible destinations from this ringoNode
     */
    private static Collection<Node> fetchDestinations() {
        return new HashSet<>(distanceVector.keySet());
    }


    /**
     * @return a Collection of this ringoNode's neighbors
     */
    private static Collection<Node> getAllKnownNeighbors() {
        return new HashSet<>(neighborsCosts.keySet());
    }


    /**
     * Get the cost of the link to go directly from this ringoNode to a neighbor
     *
     * @param neighbor
     *            is a ringoNode directly connected to this one
     * @return the link cost to go directly to this neighbor, or infinity if the
     *         specified ringoNode isn't a neighbor
     */
    private static float getCostToSingleNeighbor(Node neighbor) {
        return neighborsCosts.getOrDefault(neighbor, Float.POSITIVE_INFINITY);
    }

    /**
     * Get the cost from a neighbor to a destination, as advertised in the
     * latest message received from that neighbor
     *
     * @param neighbor is the potential next hop
     * @param destination a potential ringoNode we are trying to get to
     * @return the cost from neighbor to destination as advertised in the most
     *         recent message received from neighbor
     */
     //TEICHNER'S NOTES: M IS NULL WHEN RTT = INF
    private static float getCostFromOneArbitraryNeighborToAnother(Node neighbor, Node destination) {


        Message m = messages.get(neighbor);

        try {
            System.out.println("getCostFromOneArbitraryNeighborToAnother DESTINATION: " + destination.toString());
        } catch (Exception e) {
            System.out.println(e + ": destination is null in getCostFromOneArbitraryNeighborToAnother");
        }
        try {
            System.out.println("getCostFromOneArbitraryNeighborToAnother m data_to_be_sent value: " + m.getdataToBeSent());
        } catch (Exception e) {
            System.out.println(e + ": m is null in getCostFromOneArbitraryNeighborToAnother");
        }
        try {
            System.out.println("Neighbor in getCostFromOneArbitraryNeighborToAnother: "+neighbor.toString());
        } catch (Exception e) {
            System.out.println(e + ": either neighbor is not in messages list or neighbor is null");
        }
        if (m != null) {
            float retVal = m.getCostTo(destination);
            // return m.getCostTo(destination);
            System.out.println("getCostFromOneArbitraryNeighborToAnother RETURN VALUE: " + retVal);
            return retVal;
        } else {
            return Float.POSITIVE_INFINITY;
        }
    }
    private static float getCostFromOneArbitraryNeighborToAnother(Node neighbor, Node destination, Message aMessage) {
        Message m = messages.get(neighbor);

        //System.out.println("M in getCostFromOneArbitraryNeighborToAnother: " + m.toString());
        System.out.println("aMessage data_to_be_sent in getCostFromOneArbitraryNeighborToAnother: " + aMessage.toString());
        if (m == null) {
            messages.put(neighbor,aMessage);
        }
        Message n = messages.get(neighbor);
        try {
            System.out.println("getCostFromOneArbitraryNeighborToAnother DESTINATION: " + destination.toString());
        } catch (Exception e) {
            System.out.println(e + ": destination is null in getCostFromOneArbitraryNeighborToAnother");
        }
        try {
            System.out.println("getCostFromOneArbitraryNeighborToAnother m data_to_be_sent value: " + n.getdataToBeSent());
        } catch (Exception e) {
            System.out.println(e + ": m is null in getCostFromOneArbitraryNeighborToAnother");
        }
        try {
            System.out.println("Neighbor in getCostFromOneArbitraryNeighborToAnother: "+neighbor.toString());
        } catch (Exception e) {
            System.out.println(e + ": either neighbor is not in messages list or neighbor is null");
        }
        if (n != null) {
            float retVal = n.getCostTo(destination);
            // return m.getCostTo(destination);
            System.out.println("getCostFromOneArbitraryNeighborToAnother RETURN VALUE: " + retVal);
            return retVal;
        } else {
            return Float.POSITIVE_INFINITY;
        }
    }


    /**
     * Get the next hop listed in this nodes forwardingTable to get to a
     * specified destination
     *
     * @param destination is another Node in the Network
     * @return the Node that is the next hop to get from this ringoNode to the
     *         specified destination
     */
    private static Node fetchNextHopTo(Node destination) {
        return forwardingTable.get(destination);
    }


    /**
     * Get the current distanceVector entry from this ringoNode to a specified
     * destination
     *
     * @param destination is another Node in the Network
     * @return the cost to get from this Node to destination in our current
     *         distanceVector
     */
    private static float findCostToDestination(Node destination) {
        return distanceVector.get(destination);
    }


    /**
     * Updates the Distance Vector
     *
     * @param destination the final node
     * @param cost the cost to destination
     * @throws Exception some error
     */
    private static void updateDistanceVector(Node destination, float cost) throws Exception {
        if (cost > 0) {
            distanceVector.put(destination, cost);
        } else {
            throw new Exception("Costs can't be negative");
        }
    }


    /**
     * Sends distance vector messages to all neighbors of this ringoNode
     */
    private static void alertRingos() {
        // STEP 2: Fill in this method

        HashMap<Node, Float> vector = new HashMap<>();

        // Gets the ringoNode's distance vector.
        for (Node destination : fetchDestinations()) {
            vector.put(destination, findCostToDestination(destination));
        }
        // (Not doing poisoned reverse in this implementation)

        // Compiles the ringoNode's distance vector.
        Message message = new Message(ringoNode, vector, getAllKnownNeighbors());
        try {
            System.out.println("ringoNode in alertRingos: " + ringoNode.toString());
        } catch (Exception e) {
            System.out.println(e + ": ringoNode is null inside alertRingos");
        }
        int counter = 0;
        try {
            Set<Node> vectorKeySet = vector.keySet();
            StringBuilder sBuilder = new StringBuilder();
            Float vectorStorage = 0.0f;
            for (Node aNode: vectorKeySet) {
                vectorStorage = vector.get(aNode);
                sBuilder.append("<");
                sBuilder.append(aNode.toString());
                sBuilder.append(",");
                sBuilder.append(vectorStorage);
                sBuilder.append(">\n");
                counter++;
            }
            System.out.println("vector in alertRingos: " + sBuilder.toString());
        } catch (Exception e) {
            System.out.println(e + ": at interation " + counter + ", aNode did not have a key in the vector in alertRingos");
        }
        try {
            System.out.println("getNeigbors as called by alertRingos: "+getAllKnownNeighbors().toString());
        } catch (Exception e) {
            System.out.println(e + "getAllKnownNeighbors was null in alertRingos");
        }

        // Send the message to every neighbor.
        sendMessage(message);
    }


    /**
     * print the latest Message
     *
     * can be converted to show_matrix or show_ringo
     */
    private static void printLatestMessages() {
        System.out.println("Latest messages received by node " + ringoNode + ":");
        System.out.println("      \t| Neighbors");
        System.out.print("Dest. \t|");
        Collection<Node> neighbors = getAllKnownNeighbors();
        for (Node n : neighbors) {
            System.out.print("\t" + n);
        }
        System.out.print("\n");
        System.out.print("---------------");
        for (int i = 0; i < neighbors.size(); i++) {
            System.out.print("--------");
        }
        System.out.print("\n");
        for (Node dest : fetchDestinations()) {
            System.out.print(dest + "\t|");
            for (Node n : neighbors) {
                String costFromNeighborTo = "";
                if (getCostFromOneArbitraryNeighborToAnother(n, dest) == Float.POSITIVE_INFINITY) {
                    costFromNeighborTo = "Inf";
                } else {
                    // costFromNeighborTo =
                    // Float.toString(getCostFromOneArbitraryNeighborToAnother(n,dest));
                    costFromNeighborTo = Integer
                            .toString((int) getCostFromOneArbitraryNeighborToAnother(n, dest));
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
    private static void printVectorMatrix() {
        System.out.println("Distance vector and forwarding table for node "
                + ringoNode.toString() + ":");
        System.out.println("Dest.\tCost (Next Hop)");
        System.out.println("-------------------------");
        for (Node dest : fetchDestinations()) {
            String costToDestination;
            if (findCostToDestination(dest) == Float.POSITIVE_INFINITY) {
                costToDestination = "Inf";
            } else {
                costToDestination = Integer
                        .toString((int) findCostToDestination(dest));
            }
            System.out.println(dest.toString() + "\t" + costToDestination + " ("
                    + fetchNextHopTo(dest) + ")");
        }
        System.out.println("");
    }


    /**
     * get the current ringo's ip address
     *
     * @return the current ringo's ip address
     */
    private static String getMyIP() {
       try {
           URL url = new URL("http://bot.whatismyipaddress.com");
           BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
           return reader.readLine().trim();
       } catch (Exception e) {
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
     * @param ipAddress the target node ip address
     * @return the rtt
     */
    private static float calculate_rtt(String ipAddress, int port) {
        float start = System.currentTimeMillis();
        //Create IP UDP connection
        sender_thread.submit(new StringSender("ping:" + ringoNode.getIp() + ":" + ringoNode.getPort(), ipAddress, port));

        try {
            TimeUnit.MILLISECONDS.sleep(2000);
        } catch (InterruptedException e) {
            System.out.println("sleep fail");
        }
        System.out.println("START: " + start + "\nEND: " + pingAck + "\nRTT: " + (pingAck-start));
        return (pingAck - start);
    }
}
