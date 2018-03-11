//package edu.gatech.cc.cs3251.ateichner3.edu.gatech.cc.cs3251.ateichner3.project1;

import java.net.*;
import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * Concurrent TCP server for calculating RPN values
 */
public class rpnserverTCP {

    /**
     * Main method for the server. CLI Arguments required, see below.
     *
     * Skeleton of server code not my own, courtesy of
     * https://codereview.stackexchange.com/questions/7609/concurrent-programming-with-a-simple-server
     *
     * @param args commmand line arguments, to be formatted as
     *             java rpnserverTCP  [port number]
     *
     *             NOTE: port number should be an integer
     *
     */
    public static void main(String[] args) {
        if (args.length == 1) {
            int portNumber = Integer.parseInt(args[0]);
            try {
                final ExecutorService service = Executors.newCachedThreadPool();

                ServerSocket serverSocket = new ServerSocket(portNumber);
                while (true) {
                    Socket socket = serverSocket.accept();
                    service.submit(new ServerClass(socket));
                }
            } catch (IOException e) {
                System.out.println("FATAL ERROR: INTERNAL SERVER ERROR, SHUTTING DOWN...");
                System.exit(-1);
            }
        } else {
            System.out.println("Usage: rpnserverTCP <Port number as integer>");
        }
    }

     static class ServerClass implements Runnable {
        Socket s;
        public ServerClass(Socket socket) {
            s = socket;
        }

        public void run() {
            int total = 0;
            try {
                DataInputStream in = new DataInputStream(s.getInputStream());
                DataOutputStream out = new DataOutputStream(s.getOutputStream());

                StringBuffer inputBuffer = new StringBuffer();

                rpnProtocol protocol = new rpnProtocol();

                String input;
                //Need some sort of loop in while condition
                while ((input = in.readLine()) != null) {
                    inputBuffer.append(input);
                    //Remove below after testing
                    System.out.println(input);
                }

                String[] rpn = inputBuffer.toString().split(" ");
                String result = protocol.evaluateRpn(rpn);

                if (result.contains("ERROR")) {
                    out.writeChars(result);

                } else if (result.contains("OK ANOTHER")){
                    String[] resultArr = result.split(",");
                    try {
                        total = Integer.parseInt(resultArr[0]);
                        out.writeChars("ACK ANOTHER " + total);
                    } catch (NumberFormatException nfe1) {
                        System.out.print("Number Format Exception: " + nfe1);
                        out.writeChars("NACK (ERROR: MALFORMED ARGUMENT, BROKEN SERVER CODE)");
                    }
                } else if (result.contains("OK END")) {
                    String[] resultArr = result.split(",");
                    try {
                        total = Integer.parseInt(resultArr[0]);
                        out.writeChars("ACK END " + total);
//                        out.write(total);

                    } catch (NumberFormatException nfe2) {
                        System.out.print("NUMBER FORMAT  Exception: " + nfe2);
                        out.writeChars("NACK (ERROR: MALFORMED ARGUMENT, BROKEN SERVER CODE) FIN");
//                        out.writeChars("FIN");
                    }
                }
                s.close();
            } catch (MalformedURLException me){
                System.out.println("Malformed URL: " + me);
            } catch (IOException ioe) {
               System.out.println("IOException: " + ioe);
            }
        }
    }

    static class rpnProtocol {

        private static final int WAITING = 0;
        private static final int SENTRESULT = 1;
        private static final int SENTERROR = 2;
        private static final int ANOTHER = 3;

        private int state = WAITING;

        public String evaluateRpn(String[] rpn) {
            switch (state) {
                case WAITING:
                    return calculate_result(rpn);
                case ANOTHER:
                    return calculate_result(rpn);
                case SENTRESULT:
                    state = WAITING;
                    break;
                case SENTERROR:
                    state = WAITING;
                    break;
                default:
                    state = WAITING;
                    break;
            }
            return "WAITING";
        }

        /**
         * calculate_result() takes in a String[] formatted using the specifications below and performs a calculation.
         * The protocol state machine is then updated to reflect the current state,
         * @param rpn the String[] containing the component parts of the query
         *            rpn[0]: the first operand (integer 1)
         *            rpn[1]: the operator (addition, subtraction, etc)
         *            rpn[2]: the second operand (integer 2)
         *            rpn[3]: either the String "ANOTHER" (all-caps required) to signify another or "END" to signify end
         * @return currentResult the result of the operation
         */
        private String calculate_result(String[] rpn) {
            int currentResult = 0;
            if (rpn.length == 4) {
                switch (rpn[1]) {
                    case "+":
                        currentResult = Integer.parseInt(rpn[0]) + Integer.parseInt(rpn[2]);
                        break;
                    case "-":
                        currentResult = Integer.parseInt(rpn[0]) - Integer.parseInt(rpn[2]);
                        break;
                    case "*":
                        currentResult = Integer.parseInt(rpn[0]) * Integer.parseInt(rpn[2]);
                        break;
                    case "/":
                        currentResult = Integer.parseInt(rpn[0]) / Integer.parseInt(rpn[2]);
                        break;
                    case "x":
                        currentResult = Integer.parseInt(rpn[0]) / Integer.parseInt(rpn[2]);
                        break;
                    case "X":
                        currentResult = Integer.parseInt(rpn[0]) * Integer.parseInt(rpn[2]);
                        break;
                    default:
                        state = SENTERROR;
                        String currentResultStr = Integer.toString(currentResult);
                        return (currentResultStr + ", ERROR: INVALID OPERATOR, PROTOCOL NOT FOLLOWED");
                }
                switch (rpn[3]) {
                    case "ANOTHER": {
                        state = ANOTHER;
                        String currentResultStr = Integer.toString(currentResult);
                        return (currentResultStr + ", OK ANOTHER");
                    }
                    case "END": {
                        state = SENTRESULT;
                        String currentResultStr = Integer.toString(currentResult);
                        return (currentResultStr + ", OK END");
                    }
                    default: {
                        currentResult = 0;
                        state = SENTERROR;
                        String currentResultStr = Integer.toString(currentResult);
                        return (currentResultStr + ", ERROR: INVALID NEXT STATE INSTRUCTION, PROTOCOL NOT FOLLOWED");
                    }
                }
            } else {
                state = SENTERROR;
                String currentResultStr = Integer.toString(currentResult);
                return (currentResultStr + ", " + "ERROR: INDEX OUT OF BOUNDS, PROTOCOL NOT FOLLOWED.");
            }
        }
    }
}

