package core;

import entity.Message;
import entity.TransferState;
import network.Messaging;
import state.HostState;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.*;
import java.util.regex.Pattern;

public class CLI implements Runnable{

    HostState hostState;
    Thread leaderDiscoverThread;
    private final int LEADER_TIMEOUT = 10000;
    CLI(HostState hs) { hostState = hs; }

    @Override
    public void run() {
        Scanner sc = new Scanner(System.in);
        InetAddress localIP = hostState.getLocalIP();
        String ipv4_pattern = "/(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])";
//            Use this pattern for VPN with appropriate prefix for IP, needed coz it was not possible to communicate over VPN to a local IP address 192.****
//            String ipv4_pattern = "/25\\.(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){2}([01]?\\d\\d?|2[0-4]\\d|25[0-5])";

        Pattern IPV4_PATTERN = Pattern.compile(ipv4_pattern, Pattern.CASE_INSENSITIVE);
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
                    if (!address.isLoopbackAddress() && IPV4_PATTERN.matcher(address.toString()).matches()) {
                        localIP = address;
                    }
                }
            }
        }catch (Exception e) {

        }
        while(true){
            System.out.println(" list -l(optional for local): to list all files");
            System.out.println(" get <space sep file list>: to download");
            System.out.println(" exit: to exit");
            String line = sc.nextLine();
            if(line.compareTo("exit")==0) break;
            else {
                List<String> commands = Arrays.asList(line.split(" "));
                switch(commands.get(0)){
                    case "list": {
                        if(commands.size()==2){
                            Set<String> files = new TreeSet<>();
                            for (final File fileEntry : new File("./files").listFiles()) {
                                if (!fileEntry.isDirectory()) {
                                    files.add(fileEntry.getName());
                                }
                            }
                            System.out.println(files);
                        }
                        else{
                            try {
                                if(hostState.getLeader().equals(hostState.getLocalIP())) System.out.println(hostState.getIndex().getFiles());
                                else{
                                    Messaging.unicast(hostState.getLeader(), MessageFactory.getMessage(localIP, Message.MessageType.FILE_LIST_QUERY));
                                    long startTime = System.currentTimeMillis();
                                    long endTime = System.currentTimeMillis();
                                    while(!Thread.interrupted() && (endTime - startTime < LEADER_TIMEOUT)) {
                                        Thread.yield();
                                        endTime = System.currentTimeMillis();
                                    }
                                    if((endTime - startTime >= LEADER_TIMEOUT)) {
                                        //leader is inactive, start election
                                        System.out.println("Leader node is inactive, please try again in a few seconds");
                                        leaderDiscoverThread = new Thread(new LeaderDiscovery(hostState));
                                        leaderDiscoverThread.start();
                                    }
                                }
                            }
                            catch (IOException e){

                            }
                        }
                        break;
                    }
                    case "get":{
                        try {
                            Set<String> files = new HashSet<>(commands.subList(1,commands.size()));
                            if(!hostState.getLeader().equals(hostState.getLocalIP())){
                                Messaging.unicast(hostState.getLeader(), MessageFactory.getMessage(localIP, Message.MessageType.FILE_QUERY, files ));
                                long startTime = System.currentTimeMillis();
                                long endTime = System.currentTimeMillis();
                                while(!Thread.interrupted() && (endTime - startTime < LEADER_TIMEOUT)) {
                                    Thread.yield();
                                    endTime = System.currentTimeMillis();
                                }
                                if((endTime - startTime >= LEADER_TIMEOUT)) {
                                    //leader is inactive, start election
                                    System.out.println("Leader node is inactive, please try again in a few seconds");
                                    leaderDiscoverThread = new Thread(new LeaderDiscovery(hostState));
                                    leaderDiscoverThread.start();
                                }
                            } else {
                                Map<String,Set<InetAddress>> hostsMap = hostState.getIndex().getHostsMap(files);
                                for(String file : hostsMap.keySet()){
                                    for(InetAddress i : hostsMap.get(file))
                                        hostState.getIndex().add(file, i);
                                    hostState.getTransfers().put(file, new TransferState(new Thread(new FileTransfer(hostState.getIndex().get(file), file, FileTransfer.TransferType.RECIEVER, hostState)), false));
                                }
                            }
                            //Check where a file is available
                            Set<String> missingFiles = new TreeSet<>();
                            Set<String> locatedFiles  = hostState.getIndex().getFiles();

                            for(String file : files){
                                if(!locatedFiles.contains(file)){
                                    missingFiles.add(file);
                                }
                            }
                            System.out.println("Files : " + missingFiles + " were not available");
                        }
                        catch (IOException e){

                        }
                    }
                }
            }
        }
    }
}
