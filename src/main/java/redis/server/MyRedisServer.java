package redis.server;

import redis.server.command.Parser;
import redis.server.context.ServerContext;
import redis.server.exception.ServerStartFailedException;
import redis.server.replica.IReplicationHandler;
import redis.server.storage.Cache;
import redis.server.task.CommandTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class MyRedisServer {
    private final ServerSocket serverSocket;

    private final Cache cache;
    private final Parser parser;
    private ServerContext context;

    private final ExecutorService executorService;

    private IReplicationHandler replicationHandler;

    private final ScheduledExecutorService scheduledExecutorService;

    public MyRedisServer(ServerContext serverContext, IReplicationHandler replicationHandler, Cache cache, Parser parser, ExecutorService executorService, ScheduledExecutorService scheduledExecutorService) {
        this.executorService = executorService;
        this.scheduledExecutorService = scheduledExecutorService;
        try {
            this.context = serverContext;
            this.serverSocket = new ServerSocket(serverContext.getPort());
            this.cache = cache;
            this.parser = parser;
            this.serverSocket.setReuseAddress(true);

            this.replicationHandler = replicationHandler;

        } catch (SocketException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new ServerStartFailedException("Failed to start server: " +
                    e.getMessage());
        }
    }

    public Socket accept() throws IOException {
        return serverSocket.accept();
    }

    public void start() {
        Socket socket = null;
        try {
            while (true) {
                if (!((socket = accept()) != null)) {
                    break;
                }
                //System.out.println("starting command for server type isMaster: " + context.isMaster());
                executorService.submit(new CommandTask(socket, new BufferedReader(new InputStreamReader(socket.getInputStream())), parser, cache, context, false, scheduledExecutorService));
            }
        } catch (IOException ex) {
            System.out.println("IOException: " + ex.getMessage());
        } catch (RuntimeException ex) {
            System.out.println("RuntimeException: " + ex.getMessage());
        } finally {
            try {
                if (socket != null) {
                    socket.close();
                }
                if (executorService != null) {
                    executorService.shutdown();
                }
            } catch (IOException e) {
                System.out.println("IOException: " + e.getMessage());
            }
        }
    }
}

