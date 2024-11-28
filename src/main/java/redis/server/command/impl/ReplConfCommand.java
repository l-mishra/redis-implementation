package redis.server.command.impl;

import redis.server.command.Command;
import redis.server.command.CommandExecutionContext;
import redis.server.context.ServerContext;
import redis.server.replica.Replica;
import redis.server.replica.context.SlaveConnectionHandler;

import java.io.IOException;

public class ReplConfCommand extends Command {

    @Override
    public void executeCommand(CommandExecutionContext executionContext) throws IOException {
        if (executionContext.getCommands().size() > 1) {
            System.out.println("In REPlCommand, Commands are : " + executionContext.getCommands());
            String command = (String) executionContext.getCommands().get(4);
            if ("GETACK".equalsIgnoreCase(command) && executionContext.isChangePropagation()) {
                System.out.println("in GET ACK BLOCK");
                ServerContext.isInitialOffset = false;
                int currentProcessedOffset = executionContext.getProcessedOffset();
                executionContext.write("*3\r\n$8\r\nREPLCONF\r\n$3\r\nACK\r\n$" + ("" + currentProcessedOffset).length() + "\r\n" + currentProcessedOffset + "\r\n");
                executionContext.getOutputStream().flush();
                setProcessedOffset(executionContext);
            } else if ("ACK".equalsIgnoreCase(command) && !executionContext.isChangePropagation()) {
                System.out.println("Incrementing the ack Count:");
                Replica replica = SlaveConnectionHandler.getInstance().findReplica(executionContext.getClientSocket());
                String offsetString = (String) executionContext.getCommands().get(6);
                System.out.println("offsetString is: " + offsetString);
                int offset = Integer.parseInt(offsetString);
                if (replica != null) {
                    replica.setCurrentOffset(offset);
                }
                //yield null;
            }
            else if("listening-port".equalsIgnoreCase(command) || "capa".equalsIgnoreCase(command)){
                executionContext.write(formatStringRESP("OK"));
            }
        }
    }
}
