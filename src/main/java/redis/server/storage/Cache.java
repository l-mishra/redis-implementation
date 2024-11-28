package redis.server.storage;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

public class Cache {

    static class Data {
        private final String value;
        private long expiryTimestamp;

        public Data(String value, long expiryTimestamp) {
            this.value = value;
            this.expiryTimestamp = expiryTimestamp;
        }

        public Data(String value) {
            this.value = value;
            this.expiryTimestamp = -1;
        }

        @Override
        public String toString() {
            return "Data{" +
                    "value='" + value + '\'' +
                    ", expiryTimestamp=" + expiryTimestamp +
                    '}';
        }
    }

    private Map<String, Data> dataStore = null;
    private Map<String, TreeMap<String , Map<String, String>>> streamStore;
    private Map<String, String> lastEntryIdsStore;

    public Cache() {
        this.dataStore = new ConcurrentHashMap<>();
        this.streamStore = new ConcurrentHashMap<>();
        lastEntryIdsStore = new ConcurrentHashMap<>();
    }

    public void set(String key, String value, long ttl) {
        Data record = new Data(value, ttl);
        dataStore.put(key, record);
        System.out.println("data is written correctly");
    }

    public void set(String key, String value) {
        Data record = new Data(value);
        dataStore.put(key, record);
    }

    public String get(String key) {
        Data record = dataStore.get(key);
        System.out.println("printing retrieved records from get call: " + record);
        if (record != null) {
            if (record.expiryTimestamp != -1 && System.currentTimeMillis() > record.expiryTimestamp) {
                return null;
            } else {
                return record.value;
            }
        }
        return null;
    }

    public int size(){
        System.out.println("size of the cache is: " + dataStore.size());
        return this.dataStore.size();
    }

    public Set<String> keys(){
        return this.dataStore.keySet();
    }
    public void setStream(String streamKey, String entryId, List<List<String>> tuples){
        TreeMap<String, Map<String, String>> stream =  streamStore.computeIfAbsent(streamKey, k -> new TreeMap<>());
        Map<String, String> entry = new ConcurrentHashMap<>();
        for(List<String> tuple : tuples){
            entry.put(tuple.get(0), tuple.get(1));
        }
        stream.put(entryId, entry);
        lastEntryIdsStore.put(streamKey, entryId);
    }

    public boolean containsStream(String key){
        return lastEntryIdsStore.containsKey(key);
    }

    public String getLastEntryId(String streamKey){
        return lastEntryIdsStore.get(streamKey);
    }

    public TreeMap<String, Map<String, String>> getStreamByStreamId(String streamId){
        return streamStore.get(streamId);
    }
}


