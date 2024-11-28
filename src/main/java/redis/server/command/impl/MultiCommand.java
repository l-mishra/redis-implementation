package redis.server.command.impl;

import redis.server.command.Command;
import redis.server.command.CommandExecutionContext;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class MultiCommand extends Command {
    public MultiCommand(){
    }
    @Override
    public void executeCommand(CommandExecutionContext context) throws IOException, ExecutionException, InterruptedException {
        context.getServerContext().setTransactionContext(context.getClientSocket(), Boolean.TRUE);
        context.write("+OK\r\n");
    }
}
