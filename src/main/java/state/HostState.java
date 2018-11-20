package state;

import entity.FileIndex;

import java.net.InetAddress;

public class HostState {

    private boolean isLeader;
    private InetAddress leader;
    private FileIndex index;

    public HostState(){
        index = new FileIndex();
    }
}
