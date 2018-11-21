package network;

import java.io.IOException;
import java.net.*;
import java.util.Enumeration;

public class Messaging {
    public static void broadcast(String message) throws IOException {
        DatagramSocket c = new DatagramSocket();
        c.setBroadcast(true);
        //Try the 255.255.255.255 first
        try {
            System.out.println("Sending Broadcast: " + message);
            byte[] messageBytes = message.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(messageBytes, messageBytes.length, InetAddress.getByName("255.255.255.255"), 8888);
            c.send(sendPacket);
//            System.out.println(">>>Request packet sent to: 255.255.255.255 (DEFAULT)");

            // Messaging the message over all the network interfaces
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

//                    System.out.println(">>> Request packet sent to: " + broadcast.getHostAddress() + "; Interface: " + networkInterface.getDisplayName());
                }
            }
//            System.out.println(">>> Done looping over all network interfaces. Now waiting for a reply!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void unicast(InetAddress to, String message) throws IOException {
        DatagramSocket c = new DatagramSocket();
        //Try the 255.255.255.255 first
        try {
            System.out.println("Sending Unicast: " + message + " to: " + to.getHostAddress());
            byte[] bytes = message.getBytes();
            c.send(new DatagramPacket(bytes,bytes.length, to,4445));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
