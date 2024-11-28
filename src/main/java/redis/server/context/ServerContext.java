package redis.server.context;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ServerContext {
    private int port;
    private boolean isMaster;
    private int masterPort;
    private String masterHost;
    private final AtomicInteger processedOffset = new AtomicInteger();
    public static boolean isInitialOffset = true;
    public String rdbDirName = "/tmp";
    public String rdbDirFileName = "rdb.rdb";
    Map<Socket, Boolean> transactionContext;
    Map<Socket, List<List<Object>>> inProgressTransactionMap;

    private ServerContext() {
        transactionContext = new ConcurrentHashMap<>();
        inProgressTransactionMap = new ConcurrentHashMap<>();
    }

    public boolean inTransaction(Socket socket){
        return transactionContext.get(socket) == Boolean.TRUE;
    }

    public void enqueueCommands(Socket socket, List<Object> commands){
        inProgressTransactionMap.putIfAbsent(socket, new ArrayList<>());
        inProgressTransactionMap.get(socket).add(commands);
    }

    public List<List<Object>> getCommands(Socket clientSocket){
        return inProgressTransactionMap.get(clientSocket);
    }

    public void setTransactionContext(Socket inputSocket, Boolean inTransaction){
        Boolean inProgTransaction = transactionContext.get(inputSocket);
        if(Objects.nonNull(inProgTransaction) && Boolean.FALSE == inTransaction){
            transactionContext.remove(inputSocket);
            inProgressTransactionMap.remove(inputSocket);
        }
        transactionContext.put(inputSocket, inTransaction);
    }

    public void commitTransaction(Socket socket){
        this.inProgressTransactionMap.remove(socket);
        transactionContext.remove(socket);
    }

    public int getPort() {
        return port;
    }

    public boolean isMaster() {
        return isMaster;
    }

    private void setPort(int port) {
        this.port = port;
    }

    private void setMaster(boolean master) {
        isMaster = master;
    }

    public int getMasterPort() {
        return masterPort;
    }

    private void setMasterPort(int masterPort) {
        this.masterPort = masterPort;
    }

    public String getMasterHost() {
        return masterHost;
    }

    private void setMasterHost(String masterHost) {
        this.masterHost = masterHost;
    }

    public int getProcessedOffset() {
        return processedOffset.get();
    }

    public void setProcessedOffset(int deltaOffset) {
        processedOffset.set(this.getProcessedOffset() + deltaOffset);
    }

    public String getRdbDirName() {
        return rdbDirName;
    }

    public String getRdbDirFileName() {
        return rdbDirFileName;
    }


    @Override
    public String toString() {
        return "ServerContext{" +
                "port=" + port +
                ", isMaster=" + isMaster +
                ", masterPort=" + masterPort +
                ", masterHost='" + masterHost + '\'' +
                '}';
    }

    public static class ServerContextBuilder {
        private ServerContext serverContext;

        public ServerContextBuilder() {
            this.serverContext = new ServerContext();
        }

        public ServerContextBuilder setPort(int port) {
            this.serverContext.setPort(port);
            return this;
        }

        public ServerContextBuilder setMaster(boolean isMaster) {
            this.serverContext.setMaster(isMaster);
            return this;
        }

        public ServerContextBuilder setMasterHost(String masterHost) {
            this.serverContext.setMasterHost(masterHost);
            return this;
        }

        public ServerContextBuilder setMasterPort(int masterPort) {
            this.serverContext.setMasterPort(masterPort);
            return this;
        }

        public ServerContext setRdbDirName(String rdbDirName) {
            this.serverContext.rdbDirName = rdbDirName;
            return serverContext;
        }

        public ServerContext setRdbDirFileName(String rdbDirFileName) {
            this.serverContext.rdbDirFileName = rdbDirFileName;
            return serverContext;
        }

        public ServerContext build() {
            return serverContext;
        }
    }
}
