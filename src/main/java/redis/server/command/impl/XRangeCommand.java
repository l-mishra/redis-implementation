package redis.server.command.impl;

import redis.server.command.Command;
import redis.server.command.CommandExecutionContext;
import redis.server.storage.Cache;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;

public class XRangeCommand extends Command {

    public XRangeCommand(Cache cache) {
        super(cache);
    }

    @Override
    public void executeCommand(CommandExecutionContext context) throws IOException, ExecutionException, InterruptedException {
        List<Object> commands = context.getCommands();
        if (commands.size() < 6) {
            context.write("-ERR wrong number of arguments for 'XRANGE' command\r\n");
        }
        String streamKey = (String) commands.get(4);
        String startSequenceId = (String) commands.get(6);
        String endSequenceId = (String) commands.get(8);
        System.out.println("in XRANGE: startId: " + startSequenceId + " endSequenceId: " + endSequenceId);
        TreeMap<String, Map<String, String>> stream = cache.getStreamByStreamId(streamKey);
        if (Objects.isNull(stream) || stream.isEmpty()) {
            context.write("*0\r\n");
        }

        StringBuilder response = new StringBuilder();
        Map<String, Map<String, String>> range = null;
        if("+".equals(endSequenceId)){
            range = stream.tailMap(startSequenceId);
        }
        else{
            range = stream.subMap(startSequenceId, true, endSequenceId, true);
        }
        response.append("*").append(range.size()).append("\r\n");
        for (Map.Entry<String, Map<String, String>> entry : range.entrySet()) {
            String entryId = entry.getKey();
            Map<String, String> fields = entry.getValue();

            response.append("*2\r\n");
            response.append("$").append(entryId.length()).append("\r\n").append(entryId).append("\r\n");

            response.append("*").append(fields.size() * 2).append("\r\n");
            for (Map.Entry<String, String> field : fields.entrySet()) {
                response.append("$").append(field.getKey().length()).append("\r\n").append(field.getKey()).append("\r\n");
                response.append("$").append(field.getValue().length()).append("\r\n").append(field.getValue()).append("\r\n");
            }
        }
        context.write(response.toString());
    }
}
