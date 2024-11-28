package redis.server.replica.exception;

public class ReplicationException extends RuntimeException {
    public ReplicationException(Exception e) {
        super(e);
    }
}
