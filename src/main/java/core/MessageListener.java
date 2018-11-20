package core;

import com.google.gson.Gson;
import entity.Message;
import state.HostState;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class MessageListener implements Runnable {
    DatagramSocket socket;

    HostState hostState;
    Thread leaderDiscoverThread;

    MessageListener(HostState hs, Thread leThread) {
        hostState = hs;
        leaderDiscoverThread = leThread;
    }

    @Override
    public void run() {
        try {
            //Keep a socket open to listen to all the UDP trafic that is destined for this port
            socket = new DatagramSocket(8888, InetAddress.getByName("0.0.0.0"));
            socket.setBroadcast(true);

            while (true) {
                System.out.println(getClass().getName() + ">>>Ready to receive messages!");

                //Receive a packet
                byte[] recvBuf = new byte[15000];
                DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);
                socket.receive(packet);

                String message = new String(packet.getData(), 0, packet.getLength()).trim();
                //Packet received and parsed
                Gson gson = new Gson();
                Message parsedMessage = gson.fromJson(message, Message.class);

                System.out.println(parsedMessage.getType());
                //See if the packet holds the right command (message)

                byte[] sendData = "DISCOVER_FUIFSERVER_RESPONSE".getBytes();

                //Send a response
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, packet.getAddress(), packet.getPort());
                socket.send(sendPacket);

                leaderDiscoverThread.interrupt();

                System.out.println(getClass().getName() + ">>>Sent packet to: " + sendPacket.getAddress().getHostAddress());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
