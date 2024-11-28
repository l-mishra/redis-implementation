package redis.server.command.impl;

import redis.server.command.Command;
import redis.server.command.CommandExecutionContext;
import redis.server.command.Parser;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class ConfigCommand extends Command {

    @Override
    public void executeCommand(CommandExecutionContext context) throws IOException, ExecutionException, InterruptedException {
        List<Object> commands = context.getCommands();
        if (commands.size() > 1) {
            String parameter = (String) commands.get(6);
            String configType = (String) commands.get(4);
            if ("GET".equalsIgnoreCase(configType)) {
                if ("dir".equalsIgnoreCase(parameter)) {
                    context.write(Parser.writeArray(List.of(parameter, context.getServerContext().rdbDirName)));
                } else if ("dbfilename".equalsIgnoreCase(parameter)) {
                    context.write(Parser.writeArray(List.of(parameter, context.getServerContext().rdbDirFileName)));
                }
            }
        }
    }
}
