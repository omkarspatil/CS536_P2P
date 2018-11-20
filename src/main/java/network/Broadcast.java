package network;

import java.io.IOException;
import java.net.*;
import java.util.Enumeration;

public class Broadcast {
    public static String broadcast(String message, int timeout) throws IOException {
        DatagramSocket c = new DatagramSocket();
        c.setBroadcast(true);

        //Try the 255.255.255.255 first
        try {
            byte[] messageBytes = message.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(messageBytes, messageBytes.length, InetAddress.getByName("255.255.255.255"), 8888);
            c.send(sendPacket);
            System.out.println(">>>Request packet sent to: 255.255.255.255 (DEFAULT)");

            // Broadcast the message over all the network interfaces
            Enumeration interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = (NetworkInterface) interfaces.nextElement();
                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue; // Don't want to broadcast to the loopback interface
                }

                for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                    InetAddress broadcast = interfaceAddress.getBroadcast();
                    if (broadcast == null) {
                        continue;
                    }

                    // Send the broadcast package!
                    try {
                        c.send(new DatagramPacket(messageBytes, messageBytes.length, broadcast, 4445));
                    } catch (Exception e) {
                    }

                    System.out.println(">>> Request packet sent to: " + broadcast.getHostAddress() + "; Interface: " + networkInterface.getDisplayName());
                }
            }
            System.out.println(">>> Done looping over all network interfaces. Now waiting for a reply!");


            //Wait for a response
            byte[] recvBuf = new byte[15000];
            DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);
            c.setSoTimeout(timeout);

            try {
                c.receive(receivePacket);
            }
            catch (SocketTimeoutException e) {
                // timeout exception.
                System.out.println("Timeout reached for broadcast ");
                c.close();
                return null;
            }

            //We have a response
            System.out.println(">>> Broadcast response from server: " + receivePacket.getAddress().getHostAddress());

            //Check if the message is correct
            String messageResponse = new String(receivePacket.getData()).trim();
            //Close the port!
            c.close();

            return messageResponse;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
