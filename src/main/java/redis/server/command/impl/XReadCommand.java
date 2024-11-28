package redis.server.command.impl;

import redis.server.command.Command;
import redis.server.command.CommandExecutionContext;
import redis.server.storage.Cache;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;

public class XReadCommand extends Command {

    public XReadCommand(Cache cache) {
        super(cache);
    }

    @Override
    public void executeCommand(CommandExecutionContext context) throws IOException, ExecutionException, InterruptedException {
        List<Object> commands = context.getCommands();
        System.out.println("Commands in XREAD are: " + commands);
        if (commands.size() < 8) {
            System.out.println("This is command size check");
            context.write("-ERR wrong number of arguments for 'XREAD' command\r\n");
        }
        List<String> streamKeys= new ArrayList<>();
        List<String> startIds = new ArrayList<>();

        int midPoint = (commands.size() -5)/2;
        System.out.println("mid point in XREAD is: " + midPoint );

        for(int i = 6; i < 5 + midPoint; i += 2){
            streamKeys.add((String) commands.get(i));
        }

        for(int i = 5 + midPoint + 1 ; i  < commands.size(); i +=2){
            startIds.add((String) commands.get(i));
        }

        if (streamKeys.size() != startIds.size()) {
            context.write("-ERR wrong number of arguments for 'XREAD' command\r\n");
            return;
        }

        StringBuilder response = new StringBuilder();
        response.append("*").append(streamKeys.size()).append("\r\n");

        for (int i = 0; i < streamKeys.size(); i++) {
            String streamKey = streamKeys.get(i);
            String startId = startIds.get(i);

            TreeMap<String, Map<String, String>> stream = cache.getStreamByStreamId(streamKey);
            System.out.println("Stream details in XREAD are: " + stream);
            if (stream == null || stream.isEmpty()) {
                response.append("*0\r\n");
                continue;
            }

            response.append("*2\r\n$").append(streamKey.length()).append("\r\n").append(streamKey).append("\r\n");

            Map.Entry<String, Map<String, String>> entry = stream.higherEntry(startId);
            if (entry != null) {
                String entryId = entry.getKey();
                Map<String, String> fields = entry.getValue();

                response.append("*1\r\n*2\r\n$").append(entryId.length()).append("\r\n").append(entryId).append("\r\n");
                response.append("*").append(fields.size() * 2).append("\r\n");
                for (Map.Entry<String, String> field : fields.entrySet()) {
                    response.append("$").append(field.getKey().length()).append("\r\n").append(field.getKey()).append("\r\n");
                    response.append("$").append(field.getValue().length()).append("\r\n").append(field.getValue()).append("\r\n");
                }
            } else {
                //response.append("*0\r\n");
                response.append("$-1\r\n");
            }
        }
        System.out.println("written the XREAD data");
        context.write(response.toString());
    }
}
