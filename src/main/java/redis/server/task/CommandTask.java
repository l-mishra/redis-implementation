package redis.server.task;

import redis.server.command.*;
import redis.server.command.impl.DiscardCommand;
import redis.server.command.impl.ExecCommand;
import redis.server.command.impl.XReadCommand;
import redis.server.context.ServerContext;
import redis.server.storage.Cache;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CommandTask implements Runnable {

    private Socket clientSocket;
    private Parser parser;
    private ServerContext context;

    private boolean isChangePropagation;

    private BufferedReader in;

    private final ScheduledExecutorService scheduledExecutorService;

    public CommandTask(Socket socket, BufferedReader reader, Parser parser, Cache cache, ServerContext context, boolean isChangePropagation, ScheduledExecutorService scheduledExecutorService) {
        this.clientSocket = socket;
        this.parser = parser;
        this.context = context;
        this.isChangePropagation = isChangePropagation;
        this.in = reader;
        this.scheduledExecutorService = scheduledExecutorService;
    }

    @Override
    public void run() {
        try (OutputStream os = clientSocket.getOutputStream()) {
            List<Object> commands = null;
            while (!clientSocket.isClosed() && (commands = parser.parse(in)) != null) {
                String cmd = (String) commands.get(2);
                CommandExecutionContext commandExecutionContext = constructCommandExecutionContext(commands, isChangePropagation, context);
                Command commandExecutor = CommandFactory.getCommandExecutor(cmd);
                System.out.println("in CommandTask run method, isActive Transaction: " + context.inTransaction(clientSocket));
                if (!(commandExecutor instanceof ExecCommand) && !(commandExecutor instanceof DiscardCommand) && context.inTransaction(clientSocket)) {
                    context.enqueueCommands(clientSocket, commands);
                    os.write("+QUEUED\r\n".getBytes());
                } else if (commandExecutor instanceof XReadCommand && "block".equalsIgnoreCase((String) commands.get(4))) {
                    handleXReadWithBlockCommand(commandExecutor, commands, commandExecutionContext);
                } else {
                    commandExecutor.executeCommand(commandExecutionContext);
                }
            }
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        } catch (RuntimeException e) {
            System.out.println("RuntimeException: " + e.getMessage());
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public CommandExecutionContext constructCommandExecutionContext(List<Object> commands, boolean isChangePropagation, ServerContext serverContext) {
        return new CommandExecutionContext(commands, isChangePropagation, serverContext, clientSocket);
    }

    private void handleExecCommand(Command commandExecutor, List<?> commands, CommandExecutionContext commandExecutionContext) throws IOException, ExecutionException, InterruptedException {
        if (!context.inTransaction(clientSocket)) {
            commandExecutionContext.write("-ERR EXEC without MULTI\r\n");
            return;
        }
        context.setTransactionContext(clientSocket, Boolean.FALSE);
        commandExecutor.executeCommand(commandExecutionContext);
    }

    private void handleXReadWithBlockCommand(Command commandExecutor, List<Object> commands, CommandExecutionContext context) throws InterruptedException, IOException, ExecutionException {
        long timeout = 0;
        System.out.println("Commands for XREAD in CommandTask is: " + commands);
        if (Long.parseLong((String) commands.get(6)) == 0) {
            timeout = 1000;
        } else {
            timeout = Long.parseLong((String) commands.get(6));
        }
        if(commands.get(commands.size() - 1).equals("$")){
            commands.set(commands.size()-1, "0-1");
        }
        System.out.println("Timeout of XREAD BLOCK command is :" + timeout);
        for(int i = 0; i < 4; i++){
            commands.remove(3);
        }
        scheduledExecutorService.schedule(new XReadExecutorHelper((XReadCommand) commandExecutor, context), timeout, TimeUnit.MILLISECONDS);
    }
}
