package redis.server.replica;

import redis.server.context.ServerContext;

import java.io.IOException;

public interface IReplicationHandler {
    void setUpReplication() throws IOException;
}
