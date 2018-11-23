package core;

import com.google.gson.Gson;
import entity.Message;

import java.net.InetAddress;
import java.util.Set;

public class MessageFactory {

    static String getMessage(Message.MessageType type){
        Gson gson = new Gson();
        switch(type){
            case LEADER_DISCOVERY:
            case CONTEST_ELECTION:
            case ELECTION_PARTICIPANT:
            case FILE_LIST_QUERY:
                return gson.toJson(new Message(type, ""));
            default:
                return "";

        }

    }

    static String getMessage(Message.MessageType type, InetAddress address){
        Gson gson = new Gson();
        switch(type){
            case DECLARE_LEADER:
                return gson.toJson(new Message(type, address.getHostAddress()));
            default:
                return "";
        }
    }

    static String getMessage(Message.MessageType type, Set<String> files){
        Gson gson = new Gson();
        switch(type){
            case FILE_LIST:
            case FILE_LIST_RESPONSE:
                return gson.toJson(new Message(type, files.toString()));
            default:
                return "";
        }
    }
}
