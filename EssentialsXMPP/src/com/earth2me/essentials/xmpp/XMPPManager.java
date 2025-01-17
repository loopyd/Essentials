package com.earth2me.essentials.xmpp;

import com.earth2me.essentials.Console;
import com.earth2me.essentials.EssentialsConf;
import com.earth2me.essentials.IConf;
import com.earth2me.essentials.utils.FormatUtil;
import net.ess3.api.IUser;
import org.bukkit.entity.Player;
import org.jivesoftware.smack.*;
import org.jivesoftware.smack.Roster.SubscriptionMode;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.util.StringUtils;

import java.io.File;
import java.util.*;
import java.util.logging.*;


/**
 * <p>XMPPManager class.</p>
 *
 * @author LoopyD
 * @version $Id: $Id
 */
public class XMPPManager extends Handler implements MessageListener, ChatManagerListener, IConf {
    private static final Logger logger = Logger.getLogger("EssentialsXMPP");
    private static final SimpleFormatter formatter = new SimpleFormatter();
    private final transient EssentialsConf config;
    private transient XMPPConnection connection;
    private transient ChatManager chatManager;
    private final transient Map<String, Chat> chats = Collections.synchronizedMap(new HashMap<>());
    private final transient Set<LogRecord> logrecords = Collections.synchronizedSet(new HashSet<>());
    private final transient IEssentialsXMPP parent;
    private transient List<String> logUsers;
    private transient Level logLevel;
    private transient boolean ignoreLagMessages = true;
    private transient Thread loggerThread;
    private transient boolean threadrunning = true;

    XMPPManager(final IEssentialsXMPP parent) {
        super();
        this.parent = parent;
        config = new EssentialsConf(new File(parent.getDataFolder(), "config.yml"));
        config.setTemplateName("/config.yml", EssentialsXMPP.class);
        reloadConfig();
    }

    boolean sendMessage(final String address, final String message) {
        if (address != null && !address.isEmpty()) {
            try {
                startChat(address);
                final Chat chat;
                synchronized (chats) {
                    chat = chats.get(address);
                }
                if (chat != null) {
                    if (!connection.isConnected()) {
                        disconnect();
                        connect();
                    }
                    chat.sendMessage(FormatUtil.stripFormat(message));
                    return true;
                }
            } catch (XMPPException ex) {
                disableChat(address);
            }
        }
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public void processMessage(final Chat chat, final Message msg) {
        // Normally we should log the error message
        // But we would create a loop if the connection to a log-user fails.
        if (msg.getType() != Message.Type.error && msg.getBody().length() > 0) {
            final String message = msg.getBody();
            switch (message.charAt(0)) {
                case '@':
                    sendPrivateMessage(chat, message);
                    break;
                case '/':
                    sendCommand(chat, message);
                    break;
                default:
                    final IUser sender = parent.getUserByAddress(StringUtils.parseBareAddress(chat.getParticipant()));
                    parent.broadcastMessage(sender, "=" + sender.getBase().getDisplayName() + ": " + message, StringUtils.parseBareAddress(chat.getParticipant()));
            }
        }
    }

    private boolean connect() {
        final String server = config.getString("xmpp.server");
        if (server == null || server.equals("example.com")) {
            logger.log(Level.WARNING, "config broken for xmpp");
            return false;
        }
        final int port = config.getInt("xmpp.port", 5222);
        final String serviceName = config.getString("xmpp.servicename", server);
        final String xmppuser = config.getString("xmpp.user");
        final String password = config.getString("xmpp.password");
        final ConnectionConfiguration connConf = new ConnectionConfiguration(server, port, serviceName);
        String stringBuilder = "Connecting to xmpp server " + server + ":" + port + " as user " + xmppuser + ".";
        logger.log(Level.INFO, stringBuilder);
        connConf.setSASLAuthenticationEnabled(config.getBoolean("xmpp.sasl-enabled", false));
        connConf.setSendPresence(true);
        connConf.setReconnectionAllowed(true);
        connConf.setDebuggerEnabled(config.getBoolean("debug", false));
        connection = new XMPPConnection(connConf);
        try {
            connection.connect();

            connection.login(xmppuser, password, "Essentials-XMPP");
            connection.sendPacket(new Presence(Presence.Type.available, "No one online.", 2, Presence.Mode.available));

            connection.getRoster().setSubscriptionMode(SubscriptionMode.accept_all);
            chatManager = connection.getChatManager();
            chatManager.addChatListener(this);
            return true;
        } catch (XMPPException ex) {
            logger.log(Level.WARNING, "Failed to connect to server: " + server, ex);
            return false;
        }
    }

    final void disconnect() {
        if (loggerThread != null) {
            loggerThread.interrupt();
        }
        if (chatManager != null) {
            chatManager.removeChatListener(this);
            chatManager = null;
        }
        if (connection != null) {
            connection.disconnect(new Presence(Presence.Type.unavailable));
        }

    }

    final void updatePresence() {
        final int usercount;
        final StringBuilder stringBuilder = new StringBuilder();

        usercount = parent.getEss().getOnlinePlayers().size();

        if (usercount == 0) {
            final String presenceMsg = "No one online.";
            connection.sendPacket(new Presence(Presence.Type.available, presenceMsg, 2, Presence.Mode.dnd));
        }
        if (usercount == 1) {
            final String presenceMsg = "1 player online.";
            connection.sendPacket(new Presence(Presence.Type.available, presenceMsg, 2, Presence.Mode.available));
        }
        if (usercount > 1) {
            stringBuilder.append(usercount).append(" players online.");
            connection.sendPacket(new Presence(Presence.Type.available, stringBuilder.toString(), 2, Presence.Mode.available));
        }
    }

    /** {@inheritDoc} */
    @Override
    public void chatCreated(final Chat chat, final boolean createdLocally) {
        if (!createdLocally) {
            chat.addMessageListener(this);
            final Chat old = chats.put(StringUtils.parseBareAddress(chat.getParticipant()), chat);
            if (old != null) {
                old.removeMessageListener(this);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public final void reloadConfig() {
        logger.removeHandler(this);
        config.load();
        synchronized (chats) {
            disconnect();
            chats.clear();
            if (!connect()) {
                return;
            }
            startLoggerThread();
        }
        if (config.getBoolean("log-enabled", false)) {
            logger.addHandler(this);
            logUsers = config.getStringList("log-users");
            final String level = config.getString("log-level", "info");
            try {
                logLevel = Level.parse(level.toUpperCase(Locale.ENGLISH));
            } catch (IllegalArgumentException e) {
                logLevel = Level.INFO;
            }
            ignoreLagMessages = config.getBoolean("ignore-lag-messages", true);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void publish(final LogRecord logRecord) {
        try {
            if (ignoreLagMessages && logRecord.getMessage().equals("Can't keep up! Did the system time change, or is the server overloaded?")) {
                return;
            }
            if (logRecord.getLevel().intValue() >= logLevel.intValue()) {
                synchronized (logrecords) {
                    logrecords.add(logRecord);
                }
            }
        } catch (Exception ignored) {
            // Ignore all exceptions
            // Otherwise we create a loop.
        }
    }

    /** {@inheritDoc} */
    @Override
    public void flush() {
        // Ignore this
    }

    /** {@inheritDoc} */
    @Override
    public void close() throws SecurityException {
        // Ignore this
    }

    private void startLoggerThread() {
        loggerThread = new Thread(() -> {
            final Set<LogRecord> copy = new HashSet<>();
            final Set<String> failedUsers = new HashSet<>();
            while (threadrunning) {
                synchronized (logrecords) {
                    if (!logrecords.isEmpty()) {
                        copy.addAll(logrecords);
                        logrecords.clear();
                    }
                }
                if (!copy.isEmpty()) {
                    for (String user : logUsers) {
                        try {
                            XMPPManager.this.startChat(user);
                            for (LogRecord logRecord : copy) {
                                final String message = formatter.format(logRecord);
                                if (!XMPPManager.this.sendMessage(user, FormatUtil.stripLogColorFormat(message))) {
                                    failedUsers.add(user);
                                    break;
                                }

                            }
                        } catch (XMPPException ex) {
                            failedUsers.add(user);
                            logger.removeHandler(XMPPManager.this);
                            logger.log(Level.SEVERE, "Failed to deliver log message! Disabling logging to XMPP.", ex);
                        }
                    }
                    logUsers.removeAll(failedUsers);
                    if (logUsers.isEmpty()) {
                        logger.removeHandler(XMPPManager.this);
                        threadrunning = false;
                    }
                    copy.clear();
                }
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ex) {
                    threadrunning = false;
                }
            }
            logger.removeHandler(XMPPManager.this);
        });
        loggerThread.start();
    }

    private void startChat(final String address) throws XMPPException {
        if (chatManager == null) {
            return;
        }
        synchronized (chats) {
            if (!chats.containsKey(address)) {
                final Chat chat = chatManager.createChat(address, this);
                if (chat == null) {
                    throw new XMPPException("Could not start Chat with " + address);
                }
                chats.put(address, chat);
            }
        }
    }

    private void sendPrivateMessage(final Chat chat, final String message) {
        final String[] parts = message.split(" ", 2);
        if (parts.length == 2) {
            final List<Player> matches = parent.getServer().matchPlayer(parts[0].substring(1));

            if (matches.isEmpty()) {
                try {
                    chat.sendMessage("User " + parts[0] + " not found");
                } catch (XMPPException ex) {
                    logger.log(Level.WARNING, "Failed to send xmpp message.", ex);
                }
            } else {
                final String from = "[" + parent.getUserByAddress(StringUtils.parseBareAddress(chat.getParticipant())) + ">";
                for (Player p : matches) {
                    p.sendMessage(from + p.getDisplayName() + "]  " + message);
                }
            }
        }
    }

    private void sendCommand(final Chat chat, final String message) {
        if (config.getStringList("op-users").contains(StringUtils.parseBareAddress(chat.getParticipant()))) {
            try {
                parent.getServer().dispatchCommand(Console.getInstance().getCommandSender(), message.substring(1));
            } catch (Exception ex) {
                logger.log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
    }

    private void disableChat(final String address) {
        final Chat chat = chats.get(address);
        if (chat != null) {
            chat.removeMessageListener(this);
            chats.remove(address);
        }
    }

    /**
     * <p>isConfigValid.</p>
     *
     * @return a boolean.
     */
    public boolean isConfigValid() {
        final String server = config.getString("xmpp.server");
        return server != null && !server.equals("example.com");
    }
}
