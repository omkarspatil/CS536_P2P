package state;

import entity.FileIndex;

import java.net.InetAddress;

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

    public HostState(){
        index = new FileIndex();
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
