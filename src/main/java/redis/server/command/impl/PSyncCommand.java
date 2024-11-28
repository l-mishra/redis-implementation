package redis.server.command.impl;

import redis.server.command.Command;
import redis.server.command.CommandExecutionContext;
import redis.server.replica.context.SlaveConnectionHandler;

import java.io.IOException;
import java.util.HexFormat;
import java.util.UUID;

import static redis.server.command.impl.InfoCommand.replicationId;

public class PSyncCommand extends Command {

    @Override
    public void executeCommand(CommandExecutionContext executionContext) throws IOException {
        executionContext.write("+FULLRESYNC %s 0\r\n".formatted(replicationId));
        byte[] contents = HexFormat.of().parseHex(
                "524544495330303131fa0972656469732d76657205372e322e30fa0a72656469732d62697473c040fa056374696d65c26d08bc65fa08757365642d6d656dc2b0c41000fa08616f662d62617365c000fff06e3bfec0ff5aa2");
        executionContext.write("$" + contents.length + "\r\n");
        executionContext.writeBytes(contents);
        SlaveConnectionHandler.getInstance().set(UUID.randomUUID().toString(), executionContext.getClientSocket());
    }
}
