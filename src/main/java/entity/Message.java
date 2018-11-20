package entity;

public class Message {


    public enum MessageType {
        LEADER_DISCOVERY,
        FILE_LIST,
        CONTEST_ELECTION,
        CLAIM_LEADERSHIP,
        ACK_LEADER,
        FILE_QUERY,
        FILE_QUERY_RESPONSE,
        FILE_REQUEST,
        FILE_RESPONSE,
        FILE_404
    }

    MessageType type;
    String message;

    public Message(MessageType type, String message) {
        this.type = type;
        this.message = message;
    }

    Message(){
        super();
    }
}
