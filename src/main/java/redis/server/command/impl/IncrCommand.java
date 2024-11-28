package redis.server.command.impl;

import redis.server.command.Command;
import redis.server.command.CommandExecutionContext;
import redis.server.storage.Cache;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class IncrCommand extends Command {

    public IncrCommand(Cache cache) {
        super(cache);
    }

    @Override
    public void executeCommand(CommandExecutionContext context) throws IOException, ExecutionException, InterruptedException {
        try{
            String key = (String) context.getCommands().get(4);
            Integer val = 1;
            if(Objects.nonNull(cache.get(key))){
                val = Integer.parseInt(cache.get(key)) + 1;
            }
            cache.set(key,  "" + val);
            context.write(":" + val + "\r\n");
        }
        catch(NumberFormatException ex){
            context.write("-ERR value is not an integer or out of range\r\n");
        }

    }
}
