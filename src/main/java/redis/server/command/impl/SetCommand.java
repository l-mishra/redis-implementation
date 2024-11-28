package redis.server.command.impl;

import redis.server.command.Command;
import redis.server.command.CommandExecutionContext;
import redis.server.replica.context.SlaveConnectionHandler;
import redis.server.storage.Cache;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class SetCommand extends Command {


    public SetCommand(Cache cache) {
        super(cache);
    }

    @Override
    public void executeCommand(CommandExecutionContext executionContext) throws IOException {
        String key = (String) executionContext.getCommands().get(4);
        String val = (String) executionContext.getCommands().get(6);
        //System.out.println("Key :" + key + "  val: " + val);
        if (executionContext.isMaster() || executionContext.isChangePropagation()) {
            //System.out.println("Setting the data into the cache");
            //System.out.println("isReplica: " + !executionContext.isMaster() + " isChangePropagation: " + executionContext.isChangePropagation());
            if (executionContext.getCommands().size() > 7 &&
                    ((String) executionContext.getCommands().get(8)).equalsIgnoreCase("px")) {
                int ttl = Integer.parseInt((String) executionContext.getCommands().get(10));
                cache.set(key, val,  System.currentTimeMillis() + ttl);
            } else {
                cache.set(key, val);
            }
        }
        if (executionContext.isMaster()) {
            CompletableFuture.runAsync(() -> syncReplica(formRespCommand(executionContext.getCommands())));
        }
        if (!executionContext.isChangePropagation() && executionContext.isMaster()) {
            executionContext.write("+OK\r\n");
        }
        setProcessedOffset(executionContext);
    }

    public void syncReplica(String rawCommand) {
        SlaveConnectionHandler.getInstance().replicateCommand(rawCommand);
    }
}
