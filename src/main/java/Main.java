import org.apache.commons.cli.ParseException;
import redis.server.MyRedisServer;
import redis.server.command.CommandFactory;
import redis.server.command.Parser;
import redis.server.context.ServerContext;
import redis.server.exception.ServerStartFailedException;
import redis.server.replica.IReplicationHandler;
import redis.server.replica.impl.MasterConnectionHandler;
import redis.server.storage.Cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.nio.ByteOrder;
import java.util.concurrent.ScheduledExecutorService;

import static redis.server.constant.ServerConstant.*;

public class Main {
    public static void main(String[] args) {
        // You can use print statements as follows for debugging, they'll be visible when running tests.
        //System.out.println("Logs from your program will appear here!");
        //System.out.println("arguments lengths are: " + args.length);
        for (String arg : args) {
            System.out.println(arg);
        }
        //Uncomment this block to pass the first stage
        try {
            ServerContext serverContext = getServerContext(args);
            System.out.println(serverContext);
            Parser parser = new Parser();
            Cache cache = new Cache();
            loadRDBFile(serverContext, cache);
            CommandFactory.initialise(cache);
            ExecutorService executorService = Executors.newFixedThreadPool(10);
            ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(10);
            IReplicationHandler replicationHandler = new MasterConnectionHandler(serverContext, cache, parser, executorService, scheduledExecutorService);
            if (!serverContext.isMaster()) {
                replicationHandler.setUpReplication();
            }
            MyRedisServer redisServer = new MyRedisServer(serverContext, replicationHandler, cache, parser, executorService, scheduledExecutorService);
            redisServer.start();

        } catch (ServerStartFailedException e) {
            System.out.println("ServerStartFailedException: " + e.getMessage());
        } catch (ParseException e) {
            throw new RuntimeException("CommandLine Parser Exception: " + e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void loadRDBFile(ServerContext context, Cache cache) {
        // Get the directory and dbfilename from config
        String dir = context.getRdbDirName();
        String dbfilename = context.getRdbDirFileName();
        Path dbPath = Path.of(dir, dbfilename);
        File dbfile = new File(dbPath.toString());
        if (!dbfile.exists()) {
            System.out.println("returning as either rdbFileName or Dir is nil");
            return;
        }
        try (InputStream inputStream = new FileInputStream(dbfile)) {
            int read;
            while ((read = inputStream.read()) != -1) {
                if (read == 0xFB) {    // Start of database section
                    getLen(inputStream); // Skip hash table size info
                    getLen(inputStream); // Skip expires size info
                    break;
                }
            }
            while ((read=inputStream.read()) != -1) {
                int type = read;
                if(type == 0xFF){
                    break;
                }

                long ttl = -1;
                if (type == 0xFC) {
                    ByteBuffer buffer =
                            ByteBuffer.allocate(Long.BYTES).order(ByteOrder.LITTLE_ENDIAN);
                    buffer.put(inputStream.readNBytes(Long.BYTES));
                    buffer.flip();
                    ttl = buffer.getLong();
                    // Read the next byte for the actual value type
                    type = inputStream.read();
                }

                int keyLen = getLen(inputStream);
                byte[] keyBytes = new byte[keyLen];
                inputStream.read(keyBytes);
                String parsedKey = new String(keyBytes, StandardCharsets.UTF_8);
                int valueLen = getLen(inputStream);
                byte[] valueBytes = new byte[valueLen];
                inputStream.read(valueBytes);
                String parsedValue = new String(valueBytes, StandardCharsets.UTF_8);
                System.out.println("parsedKey : " + parsedKey + " ParsedVal: " + parsedValue + " TTL: " + ttl);
                if(ttl > 0l){
                    cache.set(parsedKey, parsedValue, ttl);
                }
                else{
                    cache.set(parsedKey, parsedValue);
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading RDB file: " + e.getMessage());
        }
    }
    private static int getLen(InputStream inputStream) throws IOException {
        int read = inputStream.read();
        int len_encoding_bit = (read & 0b11000000) >> 6;
        int len = 0;
        if (len_encoding_bit == 0) { // 6-bit length
            len = read & 0b00111111;
        } else if (len_encoding_bit ==
                1) { // 8-bit extra length (split across two bytes)
            int extra_len = inputStream.read();
            len = ((read & 0b00111111) << 8) + extra_len;
        } else if (len_encoding_bit ==
                2) { // 32-bit length (split across four bytes).
            byte[] extra_len = new byte[4];
            inputStream.read(extra_len);
            len = ByteBuffer.wrap(extra_len).getInt();
        }
        return len;
    }


    private static ServerContext getServerContext(String[] args) throws ParseException {
        int masterPort = 6379;
        String masterHost = "";
        int port = 6379;
        ServerContext.ServerContextBuilder builder = new ServerContext.ServerContextBuilder();
        boolean isMaster = true;
        int i = 0;
        while (i < args.length) {
            if (args[i].equalsIgnoreCase(PORT)) {
                i++;
                port = Integer.parseInt(args[i]);
            } else if (args[i].equals(REPLICA_ARG)) {
                isMaster = false;
                i++;
                String masterAddress = args[i];
                String[] data = masterAddress.split(" ");
                masterPort = Integer.parseInt(data[1]);
                masterHost = data[0].trim();
                builder.setMasterHost(masterHost).setMasterPort(masterPort);
            } else if (args[i].equalsIgnoreCase(RDB_FILE_NAME)) {
                i++;
                builder.setRdbDirFileName(args[i].trim());
            } else if (args[i].equalsIgnoreCase(RDB_DIR_NAME)) {
                i++;
                builder.setRdbDirName(args[i].trim());
            }
            i++;
        }
        i++;
        return builder.setPort(port).setMaster(isMaster).build();
    }
}
