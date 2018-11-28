package state;

import entity.FileIndex;
import entity.TransferState;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

public class HostState {

    private boolean isElectionHost;

    public boolean isOngoingElection() {
        return ongoingElection;
    }

    public void setOngoingElection(boolean ongoingElection) {
        this.ongoingElection = ongoingElection;
    }

    private boolean ongoingElection;
    private InetAddress leader;
    private FileIndex index;

    public Map<String, TransferState> getTransfers() {
        return transfers;
    }

    private Map<String, TransferState> transfers;


    public InetAddress getLocalIP() {
        return localIP;
    }

    public void setLocalIP(InetAddress localIP) {
        this.localIP = localIP;
    }

    private InetAddress localIP;

    public HostState(){
        index = new FileIndex();
        transfers = new HashMap<>();
    }

    public InetAddress getLeader() {
        return leader;
    }

    public void setLeader(InetAddress leader) {
        this.leader = leader;
    }

    public FileIndex getIndex() {
        return index;
    }

    public void setIndex(FileIndex index) {
        this.index = index;
    }

    public boolean isElectionHost() {
        return isElectionHost;
    }

    public void setElectionHost(boolean electionHost) {
        this.isElectionHost = electionHost;
    }
}
