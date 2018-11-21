package core;

import state.HostState;

public class HostProcess {

    public static HostState hostState;

    public static void main(String[] args){
        hostState = new HostState();
        //Start listening for messages in a separate thread
        Thread leThread = new Thread(new LeaderDiscovery(hostState));
        Thread listener = new Thread(new MessageListener(hostState, leThread));

        listener.start();
        leThread.start();

    }
}
