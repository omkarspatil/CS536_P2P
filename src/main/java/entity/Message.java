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

    private MessageType type;
    private String message;

    public MessageType getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public Message(MessageType type, String message) {
        this.type = type;
        this.message = message;
    }

    Message(){
        super();
    }
}
