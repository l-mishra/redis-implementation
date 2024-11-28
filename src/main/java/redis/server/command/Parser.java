package redis.server.command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class Parser {

    public List<Object> parse(BufferedReader in) {
        List<Object> result = new ArrayList<>();
        try {
            String line1 = in.readLine();
            if (line1 == null) {
                return null;
            }
            if (line1.charAt(0) != '*') {
                throw new RuntimeException("ERR command must be an array");
            }
            result.add(line1);
            int numElements = Integer.parseInt(line1.substring(1));
            result.add(in.readLine());
            result.add(in.readLine());
            // Read data
            String dataLine = null;
            for (int i = 1; i < numElements && (dataLine = in.readLine()) != null;
                 i++) {
                if (dataLine.isEmpty()) {
                    continue;
                }
                char type = dataLine.charAt(0);
                switch (type) {
                    case '$': // Bulk String
                        result.add(dataLine);
                        result.add(in.readLine());
                        // TODO : check bulk string length
                        break;
                    case ':':                           // Integer
                        result.add(String.valueOf(type)); // ":"
                        result.add(
                                Integer.parseInt(dataLine.substring(1))); // int value in data
                        break;
                    default:
                        break;
                }
            }
        } catch (IOException e) {
            System.out.println("Parse failed " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return result;
    }

    public static String writeSimpleString(String message) {
        return String.format("+s", message);
    }

    public static String writeBulkString(String message) {
        return String.format("$%d\r\n%s", message.length(), message);
    }

    public static String writeArray(List<Object> commands) {
        List<String> parsedCommand = new ArrayList<>();
        for (Object command : commands) {
            String cmd = (String) command;
            parsedCommand.add("$" + cmd.length());
            parsedCommand.add(cmd);
        }
        StringBuilder respCommandBuilder = new StringBuilder();
        respCommandBuilder.append(String.format("*%d", commands.size()));
        parsedCommand.forEach(command -> respCommandBuilder.append("\r\n").append(command));
        respCommandBuilder.append("\r\n");
        //System.out.println("Parsed Array Content is :" + respCommandBuilder.toString());
        return respCommandBuilder.toString();
    }

    public ReadBuffer readLine(InputStream inputStream) throws IOException {
        StringBuilder builder = new StringBuilder();
        long size = 0;
        int ch;
        while ((ch = inputStream.read()) != -1) {
            if (ch == '\r') {
                ch = inputStream.read();
                break;
            }
            size += 1;
            builder.append((char) ch);
        }
        return new ReadBuffer(builder.toString(), size);
    }

    public static class ReadBuffer {
        String line;
        long size;

        public ReadBuffer(String line, long size) {
            this.line = line;
            this.size = size;
        }

        public String getLine() {
            return line;
        }

        public void setLine(String line) {
            this.line = line;
        }

        public long getSize() {
            return size;
        }

        public void setSize(long size) {
            this.size = size;
        }
    }

    public static String writeInteger(int num) {
        return String.format(":%d\r\n", num);
    }

    public static int lengthEncoding(InputStream is, int b) throws IOException {
        int length = 100;
        int first2bits = b & 11000000;
        if (first2bits == 0) {
            System.out.println("00");
            length = 0;
        } else if (first2bits == 128) {
            System.out.println("01");
            length = 2;
        } else if (first2bits == 256) {
            System.out.println("10");
            ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
            buffer.put(is.readNBytes(4));
            buffer.rewind();
            length = 1 + buffer.getInt();
        } else if (first2bits == 256 + 128) {
            System.out.println("11");
            length = 1; // special format
        }
        return length;
    }
}
