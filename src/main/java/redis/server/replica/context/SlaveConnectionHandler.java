package redis.server.replica.context;

import redis.server.replica.Replica;

import java.io.IOException;
import java.net.Socket;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class SlaveConnectionHandler {
    private final Map<String, Replica> replicaIdToReplicaSocket;
    private static final SlaveConnectionHandler INSTANCE = new SlaveConnectionHandler();

    private static boolean prevWritesAvailable = false;

    public static SlaveConnectionHandler getInstance() {
        return INSTANCE;
    }

    public Replica get(String replicaId) {
        return replicaIdToReplicaSocket.get(replicaId);
    }

    private SlaveConnectionHandler() {
        this.replicaIdToReplicaSocket = new HashMap<>();
    }

    public void set(String replicaId, Socket replicaSocket) {
        this.replicaIdToReplicaSocket.put(replicaId, new Replica(replicaSocket));
    }


    public Collection<Replica> getAllReplicas() {
        return this.replicaIdToReplicaSocket.values();
    }

    public void replicateCommand(String respCommand) {
        prevWritesAvailable = true;
        this.getAllReplicas().forEach(replica -> {
            try {
                replica.replicateCommand(respCommand);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public int getAck(int requiredNumberOfReplica, long timeoutInMillis) {
        Instant start = Instant.now();
        int result = 0;
        if (!prevWritesAvailable) {
            return getAllReplicas().size();
        }
        getAllReplicas().forEach(replica -> {
            try {
                replica.sendAck();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        while (Duration.between(start, Instant.now()).toMillis() < timeoutInMillis) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            result = (int) getAllReplicas().stream().filter(Replica::isSyncedWithMaster).count();

            if (result >= requiredNumberOfReplica) {
                return result;
            }
        }
        prevWritesAvailable = false;
        return result;
    }

    public Replica findReplica(Socket socket) {
        return this.replicaIdToReplicaSocket.values().stream().filter
                (replica -> replica.getSocket().getPort() == socket.getPort()).findFirst().orElse(null);
    }

}
