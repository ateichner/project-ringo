//package edu.gatech.cc.cs3251.ateichner3.edu.gatech.cc.cs3251.ateichner3.project1;


import java.net.*;
import java.io.*;
import java.util.Stack;

/**
 *
 * Template code modified from
 * https://docs.oracle.com/javase/tutorial/displayCode.html?code=https://docs.oracle.com/javase/tutorial/networking/sockets/examples/KnockKnockClient.java
 */
public class rpnclientTCP {

    public static void main(String[] args) throws IOException {
        if (args.length != 3) {
            System.err.println("Usage: java rpnclientTCP <hostname> <port number> <rpn string>");
            System.exit(1);
        }
        String hostname = args[0];
        int portNumber = Integer.parseInt(args[1]);
        try (Socket rpnSocket = new Socket(hostname, portNumber);
                PrintWriter out = new PrintWriter(rpnSocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(rpnSocket.getInputStream()));) {

//            System.out.println(args[2]);
//            InputStream inStream = new ByteArrayInputStream(args[2].getBytes());
//            BufferedReader stdIn = new BufferedReader(  new InputStreamReader(inStream));

            String rpnString = args[2];
            String[] tokens= rpnString.split(" ");
//            int running_total = 0;
//            int result = 0;
            int stackSize = 0;
            Stack<String> rpnStack = new Stack<>();
            boolean lastEval = false;
//            tokens =

            while (!lastEval) {
//                rpnString = stdIn.
                try {
                    StringBuilder queryBuilder = new StringBuilder();
                    for (String token : tokens) {
                        if (token.equals("+") || token.equals("-") ||
                                token.equals("/") || token.equals("*") ||
                                token.equals("x") || token.equals("X")) {
                            if (!rpnStack.empty()) {
                                if (stackSize >= 2) {
                                    int operand2 = Integer.parseInt(rpnStack.pop());
                                    stackSize--;
                                    int operand1 = Integer.parseInt(rpnStack.pop());
                                    stackSize--;

                                    //Check if last operation
                                    if (rpnStack.isEmpty()) {
                                        queryBuilder.append(operand1).append(" ")
                                                .append(token).append(" ").append(operand2).append(" ")
                                                .append("END");
                                        System.out.println(queryBuilder.toString());
                                    } else {
                                        //Not last operation
                                        queryBuilder.append(operand1).append(" ")
                                                .append(token).append(" ").append(operand2).append(" ")
                                                .append("ANOTHER");
                                        System.out.println(queryBuilder.toString());
                                    }

                                    //Send query to server
                                    out.write(queryBuilder.toString());
                                    //Clear the builder to allow for a new query
                                    queryBuilder.delete(0, queryBuilder.length());

                                    String response = in.readLine();
                                    if (response != null) {
                                        String[] responseArr = response.split(" ");
                                        System.out.println(response);
                                        if (response.contains("ACK END")) {
                                            rpnStack.push(token);
                                            stackSize++;
                                            lastEval = true;
                                            //PUSH NUMBER ONTO STACK, DO NOT SEND ANOTHER QUERY
                                        } else if (response.contains("ACK ANOTHER")){
                                            rpnStack.push(token);
                                            stackSize++;
                                            //PUSH NUMBER ONTO STACK, SEND NEXT QUERY
                                        } else if (response.contains("NACK")){
                                            System.out.println(response);
                                            rpnSocket.close();
                                            System.exit(-1);
                                        }
                                    }
                                } else {
                                    System.err.println("ERROR: MALFORMED RPN STRING.");
                                    System.exit(-1);
                                }
                            }
                        } else {
                            //Below line should cause a catch if RPN is malformed
                            //Hacky as hell, but I need something that works, not that looks pretty

                            int potentialOperand = Integer.parseInt(token);
                            rpnStack.push(token);
                            stackSize++;
                        }
                    }
                    //IN THEORY, THIS SHOULD BE FINAL RESULT
                    System.out.println(Integer.parseInt(rpnStack.pop()));
                    stackSize--;
                } catch (NumberFormatException nfe2) {
                    System.out.println("NumberFormatException, check if RPN string formatted properly: " + nfe2);
                    break;
                }
                catch (IOException ioe1) {
                    System.out.println("IOException: " + ioe1);
                    break;
                }
            }
        } catch (IOException ioe2){
                System.out.println("IOException: " + ioe2);
        }
    }
}

//    /**
//     * This is the RPN notation splitter for the stack
//     * @param rpnString
//     * @return
//     */
//    private static String[] createQueries(String rpnString) {
//
//    }
