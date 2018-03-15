import java.net.*;
import java.util.*;

/**
 * node class responsible for defining a Ringo node
 */
public class node {
    private int PORT_NUM;
    private String poc_name;
    private int NUM_RINGO;

    private HashMap<Integer,Long> neighbor_map;
    private HashMap<String, Long> poc_to_ringo_num;
    private HashMap<String, Integer> poc_ip_port_table;

    private ArrayList<Integer> visited_list;
    private ArrayList<Integer> optimal_path;
    private ArrayList<ArrayList<Long>> rtt_matrix;

    public node(int PORT_NUM, String poc_name, int NUM_RINGO) {
        //Initialize Node
        this.PORT_NUM = PORT_NUM;
        this.poc_name = poc_name;
        this.NUM_RINGO = NUM_RINGO;

        //Create HashMaps for neighbors
        neighbor_map = new HashMap<>();
        poc_to_ringo_num = new HashMap<>();
        poc_ip_port_table = new HashMap<>();

        //Create lists for routing
        visited_list = new ArrayList<>();
        optimal_path = new ArrayList<>();

        //Create 2-D ArrayList for rtt_matrix (array list of array list of longs)
        //Each entry in the outer array list is the width of the rtt_matrix
        //Each entry in the inner array list is the height of the rtt_matrix
        //We must ensure that we traverse and add in blank entries for
        //both dimensions when a node is added
        rtt_matrix = new ArrayList<>();

        // Cost to self always 0
        add_neighbor_mapping(this.NUM_RINGO, 0);

        //Calculate cost to PoC
        long cost = this.calculate_rtt(poc_name);

        if (cost != -1) {
            //TODO Get neigbor's Ringo number via a socket
            int poc_number = 0;
//            add_poc_mapping(Integer.toString(poc_number), cost);
        } else {
            System.out.println("Neighbor unreachable, left undiscovered");
        }

        //TODO Get poc's known Ringo map via a Socket
        //
    }

    public int get_port_num() {
        return this.PORT_NUM;
    }

    public String get_poc_name() {
        return this.poc_name;
    }

    public void add_neighbor_mapping(int ringo_number, long cost) {
        neighbor_map.put(ringo_number, cost);
    }

    public void add_poc_mapping(String ip_address, int poc_num) {
//        poc_to_ringo_num.put(ip_address, poc_num);
        //PUNTED POCNUM DISCOVERY TO MAIN METHOD IN RINGO class
    }

    public HashMap<Integer,Long> get_all_neighbors() {
        return this.neighbor_map;
    }

    public HashMap<Integer,Long> get_ring() {
        //TODO: CALCULATE OPTIMAL RING BY CALLING METHOD
        //TODO: RETURN OPTIMAL RING AS A HASHMAP
        return null;
    }

    public void print_ring() {
        //TODO: flesh out print statements to print out ring
    }

    public void print_matrix() {
        //TODO: print out matrix in a readable matrix format
    }

    public ArrayList<ArrayList<Long>> get_matrix() {
        return this.rtt_matrix;
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
                return -1;
            }
        } catch ( Exception e ) {
            System.out.println("Exception:" + e.getMessage());
            return -1;
        }
    }

    private void calculate_optimal_ring() {
        //TODO: CALCULATE OPTIMAL RING BASED ON WHAT IS CURRENTLY KNOWN
    }
}
