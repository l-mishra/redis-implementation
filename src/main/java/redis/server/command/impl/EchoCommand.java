package redis.server.command.impl;

import redis.server.command.Command;
import redis.server.command.CommandExecutionContext;
import redis.server.storage.Cache;

import java.io.IOException;

public class EchoCommand extends Command {

    @Override
    public void executeCommand(CommandExecutionContext executionContext) throws IOException {
        executionContext.write(formatStringRESP((String) executionContext.getCommands().get(4)));
    }
}
