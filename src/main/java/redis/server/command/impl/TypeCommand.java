package redis.server.command.impl;

import redis.server.command.Command;
import redis.server.command.CommandExecutionContext;
import redis.server.storage.Cache;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class TypeCommand extends Command {

    public TypeCommand(Cache cache) {
        super(cache);
    }

    @Override
    public void executeCommand(CommandExecutionContext context) throws IOException, ExecutionException, InterruptedException {
        if(context.getCommands().size() < 4){
            context.write("-ERR wrong number of arguments for 'TYPE' command\r\n");
        }
        String key = (String) context.getCommands().get(4);
        System.out.println("key in Type Command is: :: " + key);
        String response;
        if((Objects.nonNull(cache.get(key)))){
            response = "+string\r\n";
        }
        else if(cache.containsStream(key)){
            response = "+stream\r\n";
        }
        else{
            response = "+none\r\n";
        }
        context.write(response);
    }
}
