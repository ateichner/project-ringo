import java.util.*;

/**
 * This class represents one node in the network and stores all information
 * available to that node, including it's ip (String), its distance vector,
 * its neighbors and the costs to get to each neighbor, and a list of the most
 * recently received message from each neighbor. In addition, the class has a
 * method to implement distance vector routing updates.
 *
 */
public class Node implements Comparable<Node>  {
    // Field to hold this node's ip
    private String ip;
    private int port;

    // Data structure mapping this nodes neighbors to the costs of getting to each neighbor.


    /**
     * Constructor for Node
     *
     * @param ip is this node's ip
     */
    Node(String ip, int port) {
        // Initialize this node's private fields
        this.ip = ip;
        this.port = port;
    }


    public String getIp() {
        return ip;
    }


    public int getPort() {
        return port;
    }


    /**
     * compare self to the target node
     *
     * @param target the Node we compare to
     * @return whether self is target
     */
    public boolean equals(Node target) {
        return ip.equals(target.getIp()) && port == target.getPort();
    }

    public int compareTo(Node node) {
        if (equals(node)){
            return 0;
        } else {
            return 1;
        }
    }
}
