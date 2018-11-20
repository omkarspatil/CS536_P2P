package core;

import entity.Message;
import network.Broadcast;

public class LeaderElection implements Runnable {
    // Find the server using UDP broadcast
    final int ELECTION_DISCOVERY_TIMEOUT = 30;

    public String discoverLeader(){
        try {
            //Find out the leader
            String leader = Broadcast.broadcast(MessageFactory.getMessage(Message.MessageType.LEADER_DISCOVERY),
                    ELECTION_DISCOVERY_TIMEOUT);
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
    }

    @Override
    public void run() {
        discoverLeader();
    }
}
