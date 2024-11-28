package redis.server.command;

import redis.server.command.impl.*;
import redis.server.storage.Cache;

public class CommandFactory {

    private static final String PING_COMMAND = "ping";
    private static final String ECHO_COMMAND = "echo";
    private static final String GET_COMMAND = "get";
    private static final String SET_COMMAND = "set";
    private static final String INFO_COMMAND = "info";
    private static final String REPLCONF_COMMAND = "replconf";
    private static final String WAIT_COMMAND = "wait";
    private static final String PSYNC_COMMAND = "psync";
    public static final String CONFIG_COMMAND = "config";
    public static final String KEYS_COMMAND = "keys";
    public static final String INCR_COMMAND = "incr";
    public static final String MULTI_COMMAND = "multi";
    public static final String EXEC_COMMAND = "exec";
    public static final String DISCARD_COMMAND = "discard";
    public static final String TYPE_COMMAND = "type";
    public static final String XADD_COMMAND = "xadd";
    public static final String XRANGE_COMMAND = "xrange";
    private static final String XREAD_COMMAND = "xread";


    private final Cache cache;
    private static CommandFactory INSTANCE;
    private static GetCommand getCommand;
    private static SetCommand setCommand;
    private static PingCommand pingCommand;
    private static InfoCommand infoCommand;
    private static ReplConfCommand replConfCommand;
    private static WaitCommand waitCommand;
    private static PSyncCommand pSyncCommand;
    private static EchoCommand echoCommand;
    private static ConfigCommand configCommand;
    private static KeysCommand keysCommand;
    private static IncrCommand incrCommand;
    private static MultiCommand multiCommand;
    private static ExecCommand execCommand;
    private static DiscardCommand discardCommand;
    private static TypeCommand typeCommand;
    private static XAddCommand xAddCommand;
    private static XRangeCommand xRangeCommand;
    private static XReadCommand xReadCommand;

    private CommandFactory(Cache cache) {
        this.cache = cache;
    }

    public static void initialise(Cache cache) {
        if (INSTANCE != null) {
            return;
        }
        synchronized (CommandFactory.class) {
            if (INSTANCE != null) {
                return;
            }
            INSTANCE = new CommandFactory(cache);
            initCommandExecutor();
        }
    }

    public static void initCommandExecutor() {
        getCommand = new GetCommand(INSTANCE.cache);
        setCommand = new SetCommand(INSTANCE.cache);
        echoCommand = new EchoCommand();
        pingCommand = new PingCommand();
        infoCommand = new InfoCommand();
        replConfCommand = new ReplConfCommand();
        pSyncCommand = new PSyncCommand();
        waitCommand = new WaitCommand();
        configCommand = new ConfigCommand();
        keysCommand = new KeysCommand(INSTANCE.cache);
        incrCommand = new IncrCommand(INSTANCE.cache);
        execCommand = new ExecCommand();
        multiCommand = new MultiCommand();
        discardCommand = new DiscardCommand();
        typeCommand = new TypeCommand(INSTANCE.cache);
        xAddCommand = new XAddCommand(INSTANCE.cache);
        xRangeCommand = new XRangeCommand(INSTANCE.cache);
        xReadCommand = new XReadCommand(INSTANCE.cache);
    }

    public static Command getCommandExecutor(String commandType) {
        if (INSTANCE == null) {
            throw new IllegalArgumentException("Initialisation is not completed yet");
        }
        switch (commandType.toLowerCase()) {
            case GET_COMMAND -> {
                return getCommand;
            }
            case SET_COMMAND -> {
                return setCommand;
            }
            case PING_COMMAND -> {
                return pingCommand;
            }
            case ECHO_COMMAND -> {
                return echoCommand;
            }
            case PSYNC_COMMAND -> {
                return pSyncCommand;
            }
            case REPLCONF_COMMAND -> {
                return replConfCommand;
            }
            case WAIT_COMMAND -> {
                return waitCommand;
            }
            case INFO_COMMAND -> {
                return infoCommand;
            }
            case CONFIG_COMMAND -> {
                return configCommand;
            }
            case KEYS_COMMAND -> {
                return keysCommand;
            }
            case INCR_COMMAND -> {
                return incrCommand;
            }
            case MULTI_COMMAND -> {
                return multiCommand;
            }
            case EXEC_COMMAND -> {
                return execCommand;
            }
            case DISCARD_COMMAND -> {
                return discardCommand;
            }
            case TYPE_COMMAND -> {
                return typeCommand;
            }
            case XADD_COMMAND -> {
                return xAddCommand;
            }
            case XRANGE_COMMAND -> {
                return xRangeCommand;
            }
            case XREAD_COMMAND -> {
                return xReadCommand;
            }
            default -> throw new IllegalArgumentException("invalid command type supplied");
        }
    }

}
