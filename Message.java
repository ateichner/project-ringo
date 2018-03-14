import java.util.*;


/**
 * A simple class to encapsulate the information transmitted from one node to
 * one of its neighbors in a distance vector routing algorithm implementation.
 *
 */
public class Message {
    private Node from;
    private HashMap<Node,Float> costMap;

    /**
     * Create a new distance vector Message
     *
     * @param from is the node sending the message
     * @param costs is a Map with the costs to all destination nodes to be included in this message
     */
    Message(Node from, Map<Node,Float> costs) {
        this.from = from;
        this.costMap = new HashMap<>(costs);
    }

    /**
     * get the incoming node
     *
     * @return the Node that sent this message
     */
    public Node getFrom() {
        return from;
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