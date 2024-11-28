package redis.server.replica;

import redis.server.command.Parser;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;

public class Replica {
    private final Socket socket;
    private int currentOffset;
    private int desiredOffset;
    private static final int REPLCONFGETACKSIZE = 37;

    public Replica(Socket socket) {
        this.socket = socket;
    }

    public int getCurrentOffset() {
        return currentOffset;
    }

    public void setCurrentOffset(int currentOffset) {
        this.currentOffset = currentOffset;
        System.out.println("current offset :" + currentOffset);
        System.out.println("desired offset :" + desiredOffset);
    }

    public Socket getSocket() {
        return socket;
    }

    public void sendAck() throws IOException {
        String command = Parser.writeArray(List.of("REPLCONF", "GETACK", "*"));
        byte[] respCommand = command.getBytes();
        OutputStream outputStream = socket.getOutputStream();
        outputStream.write(respCommand);
        outputStream.flush();
        desiredOffset += respCommand.length;
    }

    public boolean isSyncedWithMaster() {
        return desiredOffset - REPLCONFGETACKSIZE <= currentOffset;
    }

    public void replicateCommand(String command) throws IOException {
        byte[] respCommand = command.getBytes();
        this.desiredOffset += respCommand.length;
        OutputStream outputStream = socket.getOutputStream();
        outputStream.write(respCommand);
    }
}
