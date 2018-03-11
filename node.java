import java.net.*;
import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * Concurrent TCP server for calculating RPN values
 */
public class node {

    public static void main(String[] args) {
        // TODO: interface configuration

        // if (args.length == 1) {
        //     int portNumber = Integer.parseInt(args[0]);
        //     try {
        //         final ExecutorService service = Executors.newCachedThreadPool();
        //
        //         ServerSocket serverSocket = new ServerSocket(portNumber);
        //         while (true) {
        //             Socket socket = serverSocket.accept();
        //             service.submit(new ServerClass(socket));
        //         }
        //     } catch (IOException e) {
        //         System.out.println("FATAL ERROR: INTERNAL SERVER ERROR, SHUTTING DOWN...");
        //         System.exit(-1);
        //     }
        // } else {
        //     System.out.println("Usage: rpnserverTCP <Port number as integer>");
        // }
    }

    static class Sender implements Runnable {
        public Sender() {
            // TODO
        }

        public void run() {
            // TODO
        }
    }

    static class Receiver implements Runnable {
        public Receiver() {
            // TODO
        }

        public void run() {
            // TODO
        }
    }

//     static class ServerClass implements Runnable {
//         Socket s;
//         public ServerClass(Socket socket) {
//             s = socket;
//         }
//
//         public void run() {
//             int total = 0;
//             try {
//                 DataInputStream in = new DataInputStream(s.getInputStream());
//                 DataOutputStream out = new DataOutputStream(s.getOutputStream());
//
//                 StringBuffer inputBuffer = new StringBuffer();
//
//                 rpnProtocol protocol = new rpnProtocol();
//
//                 String input;
//                 //Need some sort of loop in while condition
//                 while ((input = in.readLine()) != null) {
//                     inputBuffer.append(input);
//                     //Remove below after testing
//                     System.out.println(input);
//                 }
//
//                 String[] rpn = inputBuffer.toString().split(" ");
//                 String result = protocol.evaluateRpn(rpn);
//
//                 if (result.contains("ERROR")) {
//                     out.writeChars(result);
//
//                 } else if (result.contains("OK ANOTHER")){
//                     String[] resultArr = result.split(",");
//                     try {
//                         total = Integer.parseInt(resultArr[0]);
//                         out.writeChars("ACK ANOTHER " + total);
//                     } catch (NumberFormatException nfe1) {
//                         System.out.print("Number Format Exception: " + nfe1);
//                         out.writeChars("NACK (ERROR: MALFORMED ARGUMENT, BROKEN SERVER CODE)");
//                     }
//                 } else if (result.contains("OK END")) {
//                     String[] resultArr = result.split(",");
//                     try {
//                         total = Integer.parseInt(resultArr[0]);
//                         out.writeChars("ACK END " + total);
//
//                     } catch (NumberFormatException nfe2) {
//                         System.out.print("NUMBER FORMAT  Exception: " + nfe2);
//                         out.writeChars("NACK (ERROR: MALFORMED ARGUMENT, BROKEN SERVER CODE) FIN");
//                     }
//                 }
//                 s.close();
//             } catch (MalformedURLException me){
//                 System.out.println("Malformed URL: " + me);
//             } catch (IOException ioe) {
//                System.out.println("IOException: " + ioe);
//             }
//         }
//     }
}
