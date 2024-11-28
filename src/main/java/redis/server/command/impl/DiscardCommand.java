package redis.server.command.impl;

import redis.server.command.Command;
import redis.server.command.CommandExecutionContext;
import redis.server.context.ServerContext;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class DiscardCommand extends Command {

    @Override
    public void executeCommand(CommandExecutionContext context) throws IOException, ExecutionException, InterruptedException {
        ServerContext serverContext = context.getServerContext();
        if(!serverContext.inTransaction(context.getClientSocket())){
            context.write("-ERR DISCARD without MULTI\r\n");
        }
        serverContext.setTransactionContext(context.getClientSocket(), Boolean.FALSE);
        context.write("+OK\r\n");
    }
}
