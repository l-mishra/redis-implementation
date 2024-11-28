package redis.server.replica.impl;

import redis.server.command.Parser;
import redis.server.context.ServerContext;
import redis.server.replica.IReplicationHandler;
import redis.server.storage.Cache;
import redis.server.task.CommandTask;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

public class MasterConnectionHandler implements IReplicationHandler {

    private final ServerContext serverContext;
    private volatile boolean handShakeCompleted = false;
    private final Cache cache;
    private final Parser parser;
    private final ExecutorService commandExecutor;
    private BufferedReader reader;
    private final ScheduledExecutorService scheduledExecutorService;

    public MasterConnectionHandler(ServerContext serverContext, Cache cache, Parser parser, ExecutorService executorService, ScheduledExecutorService scheduledExecutorService) {
        this.serverContext = serverContext;
        this.cache = cache;
        this.parser = parser;
        this.commandExecutor = executorService;
        this.scheduledExecutorService = scheduledExecutorService;
    }

    @Override
    public void setUpReplication() throws IOException {
        Socket masterSocket = new Socket(serverContext.getMasterHost(), serverContext.getMasterPort());
        OutputStream outputStream = masterSocket.getOutputStream();
        InputStream inputStream = masterSocket.getInputStream();
        reader = new BufferedReader(new InputStreamReader(masterSocket.getInputStream()));
        BufferedWriter writer =
                new BufferedWriter(new OutputStreamWriter(outputStream));

        writer.write("*1\r\n$4\r\nPING\r\n");
        writer.flush();
        reader.readLine();

        writer.write(
                "*3\r\n$8\r\nREPLCONF\r\n$14\r\nlistening-port\r\n$4\r\n6380\r\n");
        writer.flush();
        reader.readLine();

        writer.write("*3\r\n$8\r\nREPLCONF\r\n$4\r\ncapa\r\n$6\r\npsync2\r\n");
        writer.flush();
        reader.readLine();

        writer.write("*3\r\n$5\r\nPSYNC\r\n$1\r\n?\r\n$2\r\n-1\r\n");
        writer.flush();
        reader.readLine();

        processEmptyRDBFile(reader);
        handShakeCompleted = true;
        //dos.write(hs5.getBytes());
        applyChangePropagationFromMaster(masterSocket);
    }

    private static void processEmptyRDBFile(BufferedReader reader) throws IOException {
        if (reader.read() == -1) {
            return;
        }
        int length = Integer.parseInt(reader.readLine());
        char[] data = new char[length - 1];
        reader.read(data);
    }

    public void applyChangePropagationFromMaster(Socket masterSocket) {
        //System.out.println("Got replication command from master");
        new Thread(new CommandTask(masterSocket, reader, parser, cache, serverContext, true, scheduledExecutorService)).start();
    }

}
