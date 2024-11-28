package redis.server.command.impl;

import redis.server.command.Command;
import redis.server.command.CommandExecutionContext;
import redis.server.storage.Cache;

import java.io.IOException;

public class GetCommand extends Command {

    public GetCommand(Cache cache) {
        super(cache);
    }

    @Override
    public void executeCommand(CommandExecutionContext executionContext) throws IOException {
        String keyToFetch = (String) executionContext.getCommands().get(4);
        String value = cache.get(keyToFetch);
        System.out.println("value in get command: " + value);
        if (value != null) {
            executionContext.write(formatStringRESP(value));
        } else {
            executionContext.writeBytes("$-1\r\n".getBytes());
        }
        setProcessedOffset(executionContext);
    }
}
