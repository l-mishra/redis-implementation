package redis.server.command.impl;

import redis.server.command.Command;
import redis.server.command.CommandExecutionContext;

import java.io.IOException;

public class PingCommand extends Command {

    @Override
    public void executeCommand(CommandExecutionContext executionContext) throws IOException {
        if (!executionContext.isChangePropagation()) {
            executionContext.write("+PONG\r\n");
        }
        setProcessedOffset(executionContext);
    }
}
