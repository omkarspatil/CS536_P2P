package core;

import com.google.gson.Gson;
import entity.Message;

public class MessageFactory {

    static String getMessage(Message.MessageType type){
        Gson gson = new Gson();
        switch(type){
            case LEADER_DISCOVERY:
                return gson.toJson(new Message(type,""));

            default:
                return "";

        }

    }
}
