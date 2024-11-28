package redis.server.command;

import redis.server.command.impl.XReadCommand;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class XReadExecutorHelper implements Runnable {
    private final XReadCommand xReadCommand;
    private CommandExecutionContext context;

    public XReadExecutorHelper(XReadCommand xReadCommand, CommandExecutionContext context) {
        this.xReadCommand = xReadCommand;
        this.context = context;
    }

    @Override
    public void run() {
        try {
            xReadCommand.executeCommand(context);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }
}
