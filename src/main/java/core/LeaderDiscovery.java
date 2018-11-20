package core;

import entity.Message;
import network.Broadcast;
import state.HostState;

public class LeaderDiscovery implements Runnable {
    // Find the server using UDP broadcast
    final int ELECTION_DISCOVERY_TIMEOUT = 10000;

    HostState hostState;

    LeaderDiscovery(HostState hs) {
        hostState = hs;
    }

    public String discoverLeader(){
        try {
            //Find out the leader
            String leader = Broadcast.broadcast(MessageFactory.getMessage(Message.MessageType.LEADER_DISCOVERY),
                    ELECTION_DISCOVERY_TIMEOUT);
            System.out.println(leader);
            if(leader!=null){
                //Set the state
                return leader;
            }
            else
            {

            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return  null;
    }

    @Override
    public void run() {
        discoverLeader();
        while(!Thread.interrupted()) {
            System.out.println("G");
            Thread.yield();
        }
    }
}
