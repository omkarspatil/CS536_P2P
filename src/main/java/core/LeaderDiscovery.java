package core;

import entity.Message;
import network.Messaging;
import state.HostState;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

import static entity.Message.MessageType.*;

public class LeaderDiscovery implements Runnable {
    // Find the server using UDP broadcast
    final int LEADER_DISCOVERY_TIMEOUT = 10000;
    final int ELECTION_TIMEOUT = 10000;
    HostState hostState;

    LeaderDiscovery(HostState hs) {
        hostState = hs;
    }

    public String discoverLeader(){
        InetAddress localIP = null;
        try {

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
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            //Find out the leader
            hostState.setLeader(null);

            Messaging.broadcast(MessageFactory.getMessage(Message.MessageType.LEADER_DISCOVERY));
            long startTime = System.currentTimeMillis();

            while(!Thread.interrupted() && System.currentTimeMillis() - startTime < LEADER_DISCOVERY_TIMEOUT){
                Thread.yield();
            }

            if(hostState.getLeader()==null){
                if(!hostState.isOngoingElection()){
                    Messaging.broadcast(MessageFactory.getMessage(CONTEST_ELECTION));

                    long startElectionTime = System.currentTimeMillis();
                    hostState.setOngoingElection(true);
                    hostState.setElectionHost(true);
                    while(System.currentTimeMillis() - startElectionTime < ELECTION_TIMEOUT);

                    if(hostState.getLeader()==null){
                        hostState.setLeader(localIP);
                    }
                    //Declare the result
                    Messaging.broadcast(MessageFactory.getMessage(DECLARE_LEADER, hostState.getLeader()));
                    hostState.setOngoingElection(false);
                    hostState.setElectionHost(false);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return  null;
    }

    @Override
    public void run() {
        discoverLeader();
    }
}
