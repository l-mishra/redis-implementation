package redis.server.command;

import redis.server.storage.Cache;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

public abstract class Command {
    protected Cache cache;

    public Command(Cache cache) {
        this.cache = cache;
    }

    public Command() {
    }

    public abstract void executeCommand(CommandExecutionContext context) throws IOException, ExecutionException, InterruptedException;

    public String formatStringRESP(String response) {
        String respResponse;
        if (response == null) {
            respResponse = "$-1\r\n";
        } else {
            respResponse =
                    String.format("$%s\r\n%s\r\n", response.length(), response);
        }
        return respResponse;
    }

    public String formRespCommand(List<Object> commands) {
        String rawString = "";
        for (Object command : commands) {
            rawString += command.toString() + "\r\n";
        }
        return rawString;
    }

    public void setProcessedOffset(CommandExecutionContext executionContext) {
        if (executionContext.isChangePropagation()) {
            executionContext.setProcessedOffset(formRespCommand(executionContext.getCommands()).getBytes().length);
        }
    }

}
