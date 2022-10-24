package de.erdbeerbaerlp.lilyauth;

import de.erdbeerbaerlp.lilyauth.command.LoginCommand;
import de.erdbeerbaerlp.lilyauth.command.RegisterCommand;
import de.erdbeerbaerlp.lilyauth.config.LilyAuthConfig;
import de.erdbeerbaerlp.lilyauth.discord.DCIProxy;
import net.minecraft.server.MinecraftServer;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.UUID;

public class LilyAuth extends JavaPlugin {
    /**
     * Config folder of the plugin
     */
    public static final File pluginsFolder = new File("plugins/LilyAuth");
    /**
     * Current player login status
     */
    public final static HashMap<UUID, Boolean> loginStatus = new HashMap<>();
    /**
     * Failed user logins
     */
    public final static HashMap<UUID, Integer> failedAttempts = new HashMap<>();

    private static final HashMap<UUID, String> lastKnownIP = new HashMap<>();
    private static final HashMap<UUID, String> logins = new HashMap<>();
    private static final File loginFile = new File(pluginsFolder, "player-logins.txt");
    private static final File ipFile = new File(pluginsFolder, "player-ips.txt");


    private static void loadLogins() {
        try {
            logins.clear();
            final BufferedReader var1 = new BufferedReader(new FileReader(loginFile));
            String var2;
            while ((var2 = var1.readLine()) != null) {
                final String[] var3 = var2.split("»");
                logins.put(UUID.fromString(var3[0]), var3[1]);
            }
            var1.close();
        } catch (Exception var4) {
            Bukkit.getLogger().warning("Failed to load player logins: " + var4);
        }
    }

    private static void saveLogins() {
        try {
            final PrintWriter var1 = new PrintWriter(new FileWriter(loginFile, false));
            for (final UUID var2 : logins.keySet()) {
                final String var3 = logins.get(var2);
                var1.println(var2.toString() + "»" + var3);
            }
            var1.close();
        } catch (Exception var4) {
            Bukkit.getLogger().warning("Failed to save player logins: " + var4);
            var4.printStackTrace();
        }
    }

    private static void loadIPs() {
        try {
            lastKnownIP.clear();
            final BufferedReader r = new BufferedReader(new FileReader(ipFile));
            String str;
            while ((str = r.readLine()) != null) {
                final String[] ip = str.split("»");
                lastKnownIP.put(UUID.fromString(ip[0]), ip[1]);
            }
            r.close();
        } catch (Exception e) {
            Bukkit.getLogger().warning("Failed to load player logins: " + e);
        }
    }

    private static void saveIPs() {
        try {
            final PrintWriter w = new PrintWriter(new FileWriter(ipFile, false));
            for (final UUID uuid : lastKnownIP.keySet()) {
                final String ip = lastKnownIP.get(uuid);
                w.println(uuid.toString() + "»" + ip);
            }
            w.close();
        } catch (Exception e) {
            Bukkit.getLogger().warning("Failed to save player logins: " + e);
            e.printStackTrace();
        }
    }

    /**
     * Checks if password for a player is correct
     * @param uuid UUID of the player
     * @param password Password to check
     * @return true if password is correct, false otherwise
     */
    public static boolean checkPassword(final UUID uuid, final String password) throws NoSuchAlgorithmException {
        return logins.containsKey(uuid) && logins.get(uuid).equals(LilyAuthConfig.instance().passwordAlgorithm.crypt.hash(uuid,password));
    }

    /**
     * Register a new player
     * @param uuid UUID of the player
     * @
     */
    public static void register(final UUID uuid, final String password) throws NoSuchAlgorithmException {
        logins.put(uuid, LilyAuthConfig.instance().passwordAlgorithm.crypt.hash(uuid,password));
        saveLogins();
    }

    /**
     * Saves a last known IP for a Player
     * @param uuid UUID of the player
     * @param ip IP address of the player
     */
    public static void setLastKnownIP(final UUID uuid, final String ip){
        lastKnownIP.put(uuid,LilyAuthConfig.instance().ipAlgorithm.crypt.hash(uuid,ip));
        saveIPs();
    }

    /**
     * Checks if the ip address is the last known of that player
     * @param uuid UUID of the player
     * @param ip IP to check on
     */
    public static boolean checkLastKnownIP(final UUID uuid, final String ip){
        final String lastKnown = lastKnownIP.getOrDefault(uuid, "0.0.0.0");
        return LilyAuthConfig.instance().ipAlgorithm.crypt.hash(uuid,ip).equals(lastKnown);
    }

    /**
     * Removes the player from being registered
     * @param uuid UUID of the player
     */
    public static void unregister(final UUID uuid) {
        logins.remove(uuid);
        saveLogins();
    }

    /**
     * Checks if user is registered
     * @param uuid UUID of the player
     */
    public static boolean userExists(final UUID uuid) {
        return logins.containsKey(uuid);
    }

    /**
     * Checks if a player is authenticated
     * @param p Player
     */
    public static boolean isPlayerAuthed(final Player p) {
        return MinecraftServer.INSTANCE.configManager.isAuthed(p.getName()) || loginStatus.getOrDefault(UUIDUtils.getUUIDFromPlayer(p), false);
    }

    public static void appendFailedAttempt(final UUID player) {
        if (failedAttempts.containsKey(player)) {
            failedAttempts.put(player, failedAttempts.get(player) + 1);
        } else {
            failedAttempts.put(player, 1);
        }
    }

    public InputStream getResource(final String s) {
        return null;
    }

    public void onDisable() {
    }

    public void onEnable() {
        if (!pluginsFolder.exists())
            pluginsFolder.mkdirs();
        try {
            LilyAuthConfig.instance().loadConfig();
        } catch (IOException e) {
            Bukkit.getLogger().warning("Failed to load config");
            e.printStackTrace();
        }
        loadLogins();
        loadIPs();
        final PluginManager pm = this.getServer().getPluginManager();
        final AuthPlayerListener playerListener = new AuthPlayerListener();
        pm.registerEvent(Event.Type.PLAYER_PRELOGIN, playerListener, Event.Priority.Highest, this);
        pm.registerEvent(Event.Type.PLAYER_COMMAND_PREPROCESS, playerListener, Event.Priority.Highest, this);
        pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Event.Priority.Monitor, this);
        pm.registerEvent(Event.Type.PLAYER_CHAT, playerListener, Event.Priority.Highest, this);
        pm.registerEvent(Event.Type.PLAYER_DROP_ITEM, playerListener, Event.Priority.Highest, this);
        pm.registerEvent(Event.Type.PLAYER_INTERACT, playerListener, Event.Priority.Highest, this);
        pm.registerEvent(Event.Type.PLAYER_INTERACT_ENTITY, playerListener, Event.Priority.Highest, this);
        pm.registerEvent(Event.Type.PLAYER_PICKUP_ITEM, playerListener, Event.Priority.Highest, this);
        pm.registerEvent(Event.Type.PLAYER_BUCKET_EMPTY, playerListener, Event.Priority.Highest, this);
        pm.registerEvent(Event.Type.PLAYER_BUCKET_FILL, playerListener, Event.Priority.Highest, this);
        final PluginCommand loginCmd = this.getServer().getPluginCommand("login");
        loginCmd.setExecutor(new LoginCommand());
        final PluginCommand registerCmd = this.getServer().getPluginCommand("register");
        registerCmd.setExecutor(new RegisterCommand());
        if (pm.isPluginEnabled("DiscordIntegration")) {
            DCIProxy.registerCommands();
        }
    }
}
