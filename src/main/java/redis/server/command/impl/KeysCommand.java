package redis.server.command.impl;

import redis.server.command.Command;
import redis.server.command.CommandExecutionContext;
import redis.server.storage.Cache;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class KeysCommand extends Command {

    public KeysCommand(Cache cache) {
        super(cache);
    }

    @Override
    public void executeCommand(CommandExecutionContext executionContext) throws IOException, ExecutionException, InterruptedException {
        int size = cache.size();
        executionContext.write("*" + size + "\r\n");
        for(String key : cache.keys()){
            executionContext.write("$" + key.length() + "\r\n" + key + "\r\n");
        }
    }


}
