package core;

import com.google.gson.Gson;
import entity.Message;
import network.Messaging;
import state.HostState;

import java.io.File;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

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
            socket = new DatagramSocket(4445, InetAddress.getByName("0.0.0.0"));
            socket.setBroadcast(true);

            InetAddress localIP = null;
            for (Enumeration<NetworkInterface> ifaces =
                 NetworkInterface.getNetworkInterfaces();
                 ifaces.hasMoreElements(); )
            {
                NetworkInterface iface = ifaces.nextElement();
//                System.out.println(iface.getName() + ":");
                for (Enumeration<InetAddress> addresses =
                     iface.getInetAddresses();
                     addresses.hasMoreElements(); )
                {
                    InetAddress address = addresses.nextElement();
                    if (!address.isLoopbackAddress() && address.isSiteLocalAddress()) {
                        localIP = address;
                    }
                }
            }

            List<String> files = new ArrayList<>();
            for (final File fileEntry : new File("./files").listFiles()) {
                if (!fileEntry.isDirectory()) {
                    files.add(fileEntry.getName());
                    hostState.getIndex().add(fileEntry.getName(), localIP);
                }
            }

            while (true) {

//                System.out.println(getClass().getName() + ">>>Ready to receive messages!");
                //Receive a packet
                byte[] recvBuf = new byte[15000];
                DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);
                socket.receive(packet);

//                System.out.println("Got message from " + packet.getAddress() + " to " + localIP);
                if(packet.getAddress().equals(localIP)) {
//                    System.out.println("Self");
                    continue;
                }

                String message = new String(packet.getData(), 0, packet.getLength()).trim();
                //Packet received and parsed
                Gson gson = new Gson();
                Message parsedMessage = gson.fromJson(message, Message.class);

                Message.MessageType type = parsedMessage.getType();
                System.out.println("from " + packet.getAddress().getHostAddress() + ", to " + localIP + " Got: " + parsedMessage.getType() + " message: " + parsedMessage.getMessage());


                switch(type){
                    case DECLARE_LEADER: {
                        hostState.setLeader(InetAddress.getByName(parsedMessage.getMessage()));
                        hostState.setOngoingElection(false);
                        hostState.setElectionHost(false);
                        leaderDiscoverThread.interrupt();
                        Messaging.unicast(hostState.getLeader(),MessageFactory.getMessage(Message.MessageType.FILE_LIST,files));
                        break;
                    }
                    case LEADER_DISCOVERY: {
                        if (hostState.isOngoingElection()) {
                            if (hostState.isElectionHost()) {
                                Messaging.unicast(packet.getAddress(), MessageFactory.getMessage(Message.MessageType.CONTEST_ELECTION));
                            }
                        } else if(hostState.getLeader()!=null && hostState.getLeader().equals(InetAddress.getLocalHost())) {
                            Messaging.unicast(packet.getAddress(), MessageFactory.getMessage(Message.MessageType.DECLARE_LEADER, hostState.getLeader()));
                        }
                        break;
                    }
                    case ELECTION_PARTICIPANT:{
                        //Check if its the new bully
                        InetAddress currentLeader = hostState.getLeader();
                        if (currentLeader == null) {
                            currentLeader = localIP;
                            hostState.setLeader(localIP);
                        }
                        if(packet.getAddress().toString().compareTo(currentLeader.toString())>0){
                            hostState.setLeader(packet.getAddress());
                        }
                        break;
                    }
                    case CONTEST_ELECTION:{
                        leaderDiscoverThread.interrupt();
                        if (!hostState.isOngoingElection()) {
                            hostState.setOngoingElection(true);
                            hostState.setElectionHost(false);
                            Messaging.unicast(packet.getAddress(),MessageFactory.getMessage(Message.MessageType.ELECTION_PARTICIPANT));
                        } else if (hostState.isElectionHost()) {
                            Messaging.unicast(packet.getAddress(), MessageFactory.getMessage(Message.MessageType.CONTEST_ELECTION));
                        }
                        break;
                    }
                    case FILE_LIST:{
                        List files = gson.fromJson(parsedMessage.getMessage(), ArrayList.class);
                        for(Object file : files){
                            hostState.getIndex().add((String)file, packet.getAddress());
                        }
                        System.out.println(hostState.getIndex());
                        break;
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
