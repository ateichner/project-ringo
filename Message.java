import java.util.*;


/**
 * A simple class to encapsulate the information transmitted from one node to
 * one of its neighbors in a distance vector routing algorithm implementation.
 *
 */
public class Message {
    private Node from;
    private HashMap<Node, Float> costMap;
    private Collection<Node> destinations;

    /**
     * Create a new distance vector Message
     *
     * @param from is the node sending the message
     * @param costs is a Map with the costs to all destination nodes to be included in this message
     */
    Message(Node from, Map<Node, Float> costs, Collection<Node> destinations) {
        this.from = from;
        this.costMap = new HashMap<>(costs);
        this.destinations = destinations;
    }

    Message(String input) {
        String[] temp = input.trim().split("==");

        String fromIP = temp[0].split(":")[1].split(",")[0];
        int fromPort = Integer.parseInt(temp[0].split(":")[1].split(",")[1]);
        this.from = new Node(fromIP, fromPort);

        String[] temp2 = temp[1].split(";");

        this.costMap = new HashMap<>();
        this.destinations = null;

        for (String s: temp2) {
            String[] news = s.split(",");

            String newIP = news[0];
            int newPort = Integer.parseInt(news[1]);
            float newCost = Float.parseFloat(news[2]);

            this.costMap.put(new Node(newIP, newPort), newCost);
        }

    }

    /**
     * get the incoming node
     *
     * @return the Node that sent this message
     */
    public Node getFrom() {
        return from;
    }

    public Collection<Node> getDestinations() {
        return destinations;
    }


    public String getOutData() {
        String out = "";

        out += "from_node:" + from.getIp() + "," + Integer.toString(from.getPort()) + "==";

        for (Node n: new TreeSet<>(costMap.keySet())) {
            out += n.getIp() + "," + Integer.toString(n.getPort()) + "," + Float.toString(costMap.get(n)) + ";";
        }

        return out;
    }

    /**
     * get the cost to one destination
     *
     * @param destination is the Node we're interested in knowing the cost to
     * @return the advertised cost from the sender of this message to the specified destination
     */
    public float getCostTo(Node destination) {
        return costMap.getOrDefault(destination, Float.POSITIVE_INFINITY);
    }
}