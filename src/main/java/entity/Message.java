package entity;

import java.net.InetAddress;

public class Message {


    public enum MessageType {
        LEADER_DISCOVERY,
        FILE_LIST,
        CONTEST_ELECTION,
        DECLARE_LEADER,
        FILE_LIST_QUERY,
        FILE_LIST_RESPONSE,
        FILE_QUERY,
        FILE_QUERY_RESPONSE,
        FILE_REQUEST,
        FILE_RESPONSE,
        SEND_FILE,
        FILE_404,
        FILE_RESPONSE_404,
        ELECTION_PARTICIPANT
    }

    private MessageType type;
    private String message;
    private InetAddress localIp;

    public MessageType getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public InetAddress getLocalIp() { return localIp;}

    public Message( InetAddress localIp, MessageType type, String message) {
        this.type = type;
        this.message = message;
        this.localIp = localIp;
    }

    Message(){
        super();
    }
}
