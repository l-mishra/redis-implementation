package redis.server.command;

import redis.server.context.ServerContext;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;

public class CommandExecutionContext {

    private List<Object> commands;
    private boolean isChangePropagation;
    private final OutputStream outputStream;
    private final ServerContext serverContext;
    private final Socket clientSocket;

    public CommandExecutionContext(List<Object> commands, boolean isChangePropagation, ServerContext serverContext, Socket clientSocket) {
        this.commands = commands;
        this.isChangePropagation = isChangePropagation;
        this.serverContext = serverContext;
        this.clientSocket = clientSocket;
        try {
            outputStream = clientSocket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("failed to retrieve output stream object from socket");
        }
    }

    public List<Object> getCommands() {
        return commands;
    }


    public boolean isChangePropagation() {
        return isChangePropagation;
    }

    public void writeBytes(byte[] bytes) throws IOException {
        outputStream.write(bytes);
    }

    public void setCommands(List<Object> commands){
        this.commands = commands;
    }



    public void write(String string) throws IOException {
        outputStream.write(string.getBytes());
    }

    public boolean isMaster() {
        return serverContext.isMaster();
    }

    public void setProcessedOffset(int offsetInBytes) {
        serverContext.setProcessedOffset(offsetInBytes);
    }

    public int getProcessedOffset() {
        return serverContext.getProcessedOffset();
    }

    public ServerContext getServerContext() {
        return serverContext;
    }

    public Socket getClientSocket() {
        return clientSocket;
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }
}
