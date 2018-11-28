package entity;

public class TransferState {
    Thread thread;
    Boolean status;

    public TransferState(Thread thread, Boolean status) {
        this.thread = thread;
        this.status = status;
    }

    public Thread getThread() {
        return thread;
    }

    public void setThread(Thread thread) {
        this.thread = thread;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }
}
