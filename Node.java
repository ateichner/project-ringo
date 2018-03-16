import java.util.*;

/**
 * node class responsible for defining a Ringo node
 */
public class Node implements Comparable<Node> {
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


    /**
     * get the ip address of one node
     *
     * @return the ip address
     */
    public String getIp() {
        return ip;
    }


    /**
     * get the port number of one node
     *
     * @return the port number
     */
    public int getPort() {
        return port;
    }


    @Override
    public String toString() {
        return getIp() + "|" + getPort();
    }


    /**
     * compare self to the target node
     *
     * @param target the Node we compare to
     * @return whether self is target
     */
    private boolean equals(Node target) {
        return ip.equals(target.getIp()) && port == target.getPort();
    }


    public int compareTo(Node target) {
        if (equals(target)) {
            return 0;
        } else {
            return 1;
        }
    }
}