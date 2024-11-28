package redis.server.command.impl;

import redis.server.command.Command;
import redis.server.command.CommandExecutionContext;
import redis.server.command.CommandFactory;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class ExecCommand extends Command {
    @Override
    public void executeCommand(CommandExecutionContext context) throws IOException, ExecutionException, InterruptedException {
        if (!context.getServerContext().inTransaction(context.getClientSocket())) {
            context.write("-ERR EXEC without MULTI\r\n");
            return;
        }
        List<List<Object>> commands = context.getServerContext().getCommands(context.getClientSocket());
        if(Objects.isNull(commands)){
            context.write("*0\r\n");
        }
        else {
            context.write("*" + commands.size() + "\r\n");
            for (List<Object> command : commands) {
                Command commandExecutor = CommandFactory.getCommandExecutor((String) command.get(2));
                if (Objects.nonNull(commandExecutor)) {
                    commandExecutor.executeCommand(new CommandExecutionContext(command, context.isChangePropagation(), context.getServerContext(), context.getClientSocket()));
                }
                else{
                    context.write("-ERR unknown command '" + (String) command.get(2) + "\r\n");
                }
            }
        }
        context.getServerContext().setTransactionContext(context.getClientSocket(), Boolean.FALSE);
    }
}
