package core;

import com.google.gson.Gson;
import entity.Message;
import network.Messaging;
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
            socket = new DatagramSocket(4445, InetAddress.getByName("0.0.0.0"));
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

                Message.MessageType type = parsedMessage.getType();

                switch(type){
                    case DECLARE_LEADER: {
                        hostState.setLeader(InetAddress.getByName(parsedMessage.getMessage()));
                        hostState.setOngoingElection(false);
                        leaderDiscoverThread.interrupt();
                        //TODO: Send file list
                        break;
                    }
                    case LEADER_DISCOVERY:{
                        if(hostState.isElectionHost()){
                            Messaging.unicast(packet.getAddress(), MessageFactory.getMessage(Message.MessageType.CONTEST_ELECTION));
                        }

                        if(hostState.getLeader().equals(InetAddress.getLocalHost()))
                            Messaging.unicast(packet.getAddress(), MessageFactory.getMessage(Message.MessageType.DECLARE_LEADER, hostState.getLeader()));

                        break;
                    }
                    case ELECTION_PARTICIPANT:{
                        //Check if its the new bully
                        InetAddress currentLeader = hostState.getLeader();
                        if(currentLeader == null || packet.getAddress().toString().compareTo(currentLeader.toString())>0){
                            hostState.setLeader(packet.getAddress());
                        }
                        break;
                    }
                    case CONTEST_ELECTION:{
                        Messaging.unicast(packet.getAddress(),MessageFactory.getMessage(Message.MessageType.ELECTION_PARTICIPANT));
                        break;
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
