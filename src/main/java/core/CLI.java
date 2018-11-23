package core;

import entity.Message;
import network.Messaging;
import state.HostState;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class CLI implements Runnable{

    HostState hostState;
    CLI(HostState hs) {
        hostState = hs;
    }

    @Override
    public void run() {
        Scanner sc = new Scanner(System.in);

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
                                    Messaging.unicast(hostState.getLeader(), MessageFactory.getMessage(Message.MessageType.FILE_LIST_QUERY));
                                    while(!Thread.interrupted()) Thread.yield();
                                }
                            }
                            catch (IOException e){

                            }
                        }
                        break;
                    }
                    case "get":{
                        try {
                            if(!hostState.getLeader().equals(hostState.getLocalIP())){
                                Messaging.unicast(hostState.getLeader(), MessageFactory.getMessage(Message.MessageType.FILE_QUERY, new HashSet<>(commands.subList(1,commands.size()))));
                                while(!Thread.interrupted()) Thread.yield();
                            }
                            System.out.println(hostState.getIndex());
                        }
                        catch (IOException e){

                        }
                    }
                }
            }
        }
    }
}
