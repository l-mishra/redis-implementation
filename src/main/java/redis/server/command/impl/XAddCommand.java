package redis.server.command.impl;

import redis.server.command.Command;
import redis.server.command.CommandExecutionContext;
import redis.server.storage.Cache;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class XAddCommand extends Command {

    public XAddCommand(Cache cache) {
        super(cache);
    }

    @Override
    public void executeCommand(CommandExecutionContext context) throws IOException, ExecutionException, InterruptedException {
        List<Object> commands = context.getCommands();
        if (commands.size() < 6) {
            context.write("-ERR wrong number of arguments for 'XADD' command\r\n");
        }
        String streamKey = (String) commands.get(4);
        String entryId = (String) commands.get(6);
        if (entryId.equals("*")) {
            entryId = generateNextEntryId(streamKey);
        }
        else if(entryId.endsWith("-*")){
            entryId = generateNextEntryId(entryId, streamKey);
        }
        if(!isValidEntryId(entryId, streamKey)){
            if(entryId.equals("0-0")){
                context.write("-ERR The ID specified in XADD must be greater than 0-0\r\n");
                return;
            }
            context.write("-ERR The ID specified in XADD is equal or smaller than the target stream top item\r\n");
            return;
        }
        System.out.println("commands in XADD are: " + commands);
        List<List<String>> tuples = new ArrayList<>();
        for(int i = 8; i < commands.size(); i+= 4){
            List<String> tuple = new ArrayList<>();
            tuples.add(tuple);
            tuple.add((String) commands.get(i));
            tuple.add((String) commands.get(i+2));
        }
        cache.setStream(streamKey, entryId, tuples);
        context.write( "$" + entryId.length() + "\r\n" + entryId + "\r\n");
    }

    private String generateNextEntryId(String streamKey) {
        long millisecondsTime = System.currentTimeMillis();
        long sequenceNumber = 0;
        String lastEntryId = cache.getLastEntryId(streamKey);
        if (Objects.nonNull(lastEntryId)) {
            String[] lastIdParts = lastEntryId.split("-");
            long lastMilliseconds = Long.parseLong(lastIdParts[0]);
            long lastSequence = Long.parseLong(lastIdParts[1]);
            if (millisecondsTime == lastMilliseconds) {
                sequenceNumber = lastSequence + 1;
            }
        }
        return millisecondsTime + "-" + sequenceNumber;
    }

    private String generateNextEntryId(String entryId, String streamKey) {
        String[] idParts = entryId.split("-");
        long millisecondsTime = Long.parseLong(idParts[0]);
        long sequenceNumber = 0;

        String lastEntryId = cache.getLastEntryId(streamKey);
        if (Objects.nonNull(lastEntryId)) {
            String[] lastIdParts = lastEntryId.split("-");
            long lastMilliseconds = Long.parseLong(lastIdParts[0]);
            long lastSequenceNumber = Long.parseLong(lastIdParts[1]);

            if (millisecondsTime == lastMilliseconds) {
                sequenceNumber = lastSequenceNumber + 1;
            }
        }
        if (millisecondsTime == 0 && sequenceNumber == 0) {
            sequenceNumber = 1;
        }
        return millisecondsTime + "-" + sequenceNumber;
    }

    private boolean isValidEntryId(String entryId, String streamKey) {
        if (entryId.equals("0-0")) {
            return false;
        }

        String lastEntryId = cache.getLastEntryId(streamKey);
        if (lastEntryId == null) {
            return true;
        }

        String[] lastIdParts = lastEntryId.split("-");
        String[] newIdParts = entryId.split("-");

        long lastMilliseconds = Long.parseLong(lastIdParts[0]);
        long newMilliseconds = Long.parseLong(newIdParts[0]);

        if (newMilliseconds < lastMilliseconds) {
            return false;
        } else if (newMilliseconds == lastMilliseconds) {
            long lastSequence = Long.parseLong(lastIdParts[1]);
            long newSequence = Long.parseLong(newIdParts[1]);
            return newSequence > lastSequence;
        }

        return true;
    }
}
