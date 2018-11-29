package core;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import entity.Message;
import entity.TransferState;
import network.Messaging;
import state.HostState;

import java.io.File;
import java.lang.reflect.Type;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.*;

public class MessageListener implements Runnable {
    DatagramSocket socket;
    HostState hostState;
    Thread leaderDiscoverThread;
    Thread CLIThread;

    MessageListener(HostState hs, Thread leThread, Thread CLIThread) {
        hostState = hs;
        leaderDiscoverThread = leThread;
        this.CLIThread = CLIThread;
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

            hostState.setLocalIP(localIP);

            Set<String> files = new TreeSet<>();
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
                        //leaderDiscoverThread.interrupt();
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
                        List filesIn = gson.fromJson(parsedMessage.getMessage(), ArrayList.class);
                        for(Object file : filesIn){
                            hostState.getIndex().add((String)file, packet.getAddress());
                        }
                        System.out.println(hostState.getIndex());
                        break;
                    }
                    case FILE_LIST_QUERY:{
                        Messaging.unicast(packet.getAddress(), MessageFactory.getMessage(Message.MessageType.FILE_LIST_RESPONSE, hostState.getIndex().getFiles()));
                        break;
                    }
                    case FILE_LIST_RESPONSE:{
                        System.out.println(parsedMessage.getMessage());
                        CLIThread.interrupt();
                        break;
                    }
                    case FILE_QUERY:{
                        Set filesList = gson.fromJson(parsedMessage.getMessage(), Set.class);
                        Map<String,Set<InetAddress>> hostsMap = hostState.getIndex().getHostsMap(filesList);
                        Messaging.unicast(packet.getAddress(), MessageFactory.getMessageForQueryResponse(Message.MessageType.FILE_QUERY_RESPONSE, hostsMap));
                        break;
                    }
                    case FILE_QUERY_RESPONSE:{
                        Type mapType = new TypeToken<Map<String,Set<InetAddress>>>(){}.getType();
                        Map<String,Set<InetAddress>> hostsMap = gson.fromJson(parsedMessage.getMessage(), mapType);
                        for(String file : hostsMap.keySet()){
                            for(InetAddress i : hostsMap.get(file))
                            hostState.getIndex().add(file, i);
                            hostState.getTransfers().put(file, new TransferState(new Thread(new FileTransfer(hostState.getIndex().get(file), file, FileTransfer.TransferType.RECIEVER, hostState)), false));
                        }
                        CLIThread.interrupt();

                        for(String file : hostState.getTransfers().keySet()){
                            hostState.getTransfers().get(file).getThread().start();
                        }

                        break;
                    }
                    case FILE_404:{
                        System.out.println(parsedMessage.getMessage());
                        CLIThread.interrupt();
                        break;
                    }
                    case FILE_REQUEST:{
                        File f = new File("./files/"+parsedMessage.getMessage());
                        if(f.exists() && !f.isDirectory()) {
                            Messaging.unicast(packet.getAddress(), MessageFactory.getMessage(Message.MessageType.FILE_RESPONSE, parsedMessage.getMessage()));
                        }
                        else{
                            Messaging.unicast(packet.getAddress(), MessageFactory.getMessage(Message.MessageType.FILE_RESPONSE_404, parsedMessage.getMessage()));
                        }
                        break;
                    }
                    case FILE_RESPONSE:{
                        hostState.getTransfers().get(parsedMessage.getMessage()).setStatus(true);
                        hostState.getTransfers().get(parsedMessage.getMessage()).getThread().interrupt();
                        break;
                    }
                    case FILE_RESPONSE_404:{
                        hostState.getTransfers().get(parsedMessage.getMessage()).getThread().interrupt();
                        break;
                    }
                    case SEND_FILE:{
                        String[] parts = parsedMessage.getMessage().split(",");
                        int port = Integer.parseInt(parts[1]);
                        hostState.getTransfers().put(parts[0], new TransferState(new Thread(new FileTransfer(
                                packet.getAddress(), parts[0], FileTransfer.TransferType.SENDER, hostState, port)),false));
                        hostState.getTransfers().get(parts[0]).getThread().start();
                        break;
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
