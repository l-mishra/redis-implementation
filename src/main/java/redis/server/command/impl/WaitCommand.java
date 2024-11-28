package redis.server.command.impl;

import redis.server.command.Command;
import redis.server.command.CommandExecutionContext;
import redis.server.command.Parser;
import redis.server.replica.context.SlaveConnectionHandler;

import java.io.IOException;

public class WaitCommand extends Command {

    @Override
    public void executeCommand(CommandExecutionContext context) throws IOException {
        int expectedReplicaCount;
        long timeOutMillis;
        //System.out.println("in wait command block");
        expectedReplicaCount = Integer.parseInt(context.getCommands().get(4).toString());
        timeOutMillis = Integer.parseInt(context.getCommands().get(6).toString());
        int ackCount;
        ackCount = SlaveConnectionHandler.getInstance().getAck(expectedReplicaCount, timeOutMillis);
        System.out.println("expected replicaCount : " + expectedReplicaCount + " and timeoutMillis: " + timeOutMillis);
        context.write(Parser.writeInteger(ackCount));
    }
}

