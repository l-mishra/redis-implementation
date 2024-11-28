package redis.server.command.impl;

import redis.server.command.Command;
import redis.server.command.CommandExecutionContext;

import java.io.IOException;

public class InfoCommand extends Command {

    public static final String replicationId = "8371b4fb1155b71f4a04d3e1bc3e18c4a990aeeb";
    public static final int masterOffset = 0;

    @Override
    public void executeCommand(CommandExecutionContext executionContext) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("role:").append(executionContext.isMaster() ? "master" : "slave").append("\n");
        sb.append("master_replid:" + replicationId + "\n");
        sb.append("master_repl_offset:" + masterOffset);
        executionContext.write(formatStringRESP(sb.toString()));
        setProcessedOffset(executionContext);
    }
}
