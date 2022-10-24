package de.erdbeerbaerlp.lilyauth.config;

import com.moandjiezana.toml.Toml;
import com.moandjiezana.toml.TomlWriter;
import com.moandjiezana.toml.TomlComment;
import com.moandjiezana.toml.TomlIgnore;
import de.erdbeerbaerlp.lilyauth.LilyAuth;
import de.erdbeerbaerlp.lilyauth.crypto.MD5;
import de.erdbeerbaerlp.lilyauth.crypto.Plaintext;
import de.erdbeerbaerlp.lilyauth.crypto.SHA512;

import java.io.File;
import java.io.IOException;

public class LilyAuthConfig {
    public enum ConfAlgorithm {
        PLAINTEXT(new Plaintext()),MD5(new MD5()),SHA512(new SHA512());
        public final de.erdbeerbaerlp.lilyauth.crypto.Algorithm crypt;
        ConfAlgorithm(de.erdbeerbaerlp.lilyauth.crypto.Algorithm crypt) {
            this.crypt = crypt;
        }


    }

    @TomlIgnore
    private static LilyAuthConfig INSTANCE;
    @TomlIgnore
    private static final File configFile = new File(LilyAuth.pluginsFolder, "config.toml");

    static {
        //First instance of the Config
        INSTANCE = new LilyAuthConfig();
    }

    public static LilyAuthConfig instance() {
        return INSTANCE;
    }

    public void loadConfig() throws IOException, IllegalStateException {
        if (!configFile.exists()) {
            INSTANCE = new LilyAuthConfig();
            INSTANCE.saveConfig();
            return;
        }
        INSTANCE = new Toml().read(configFile).to(LilyAuthConfig.class);
        INSTANCE.saveConfig(); //Re-write the config so new values get added after updates
    }

    public void saveConfig() throws IOException {
        if (!configFile.exists()) {
            if (!configFile.getParentFile().exists()) configFile.getParentFile().mkdirs();
            configFile.createNewFile();
        }
        final TomlWriter w = new TomlWriter.Builder()
                .indentValuesBy(2)
                .indentTablesBy(4)
                .padArrayDelimitersBy(2)
                .build();
        w.write(this, configFile);
    }

    @TomlComment({"Determines how to store passwords, changing this will invalidate all saved ones!","Available: PLAINTEXT (not recommended), MD5, SHA512"})
    public ConfAlgorithm passwordAlgorithm = ConfAlgorithm.SHA512;
    @TomlComment("Save a player's last IP address to allow instant login?")
    public boolean savePlayerIPs = true;
    @TomlComment({"Determines how to store IPs, changing this will invalidate all saved ones!","Available: PLAINTEXT, MD5, SHA512"})
    public ConfAlgorithm ipAlgorithm = ConfAlgorithm.PLAINTEXT;

    @TomlComment("Message configuration")
    public Messages messages = new Messages();
    public static class Messages {

        public String loggedInWithIP = "§aYou have been logged in with your last known IP address";
        public String noPassword = "§cNo password supplied!";
        public String loggedIn = "§aYou have been logged in!";
        public JoinMessage joinMessage = new JoinMessage();
        @TomlComment("Message for being banned by bruteforcing an password")
        public String bruteforceBanMessage = "You have been banned for bruteforcing.\nContact server owner for more info";

        public String[] cannotChatMessage = new String[]{"§cYou are not Authenticated! Cannot chat!", "§aIf you have issues logging in, contact the server owner"};
        public String[] notAuthentificatedJoinMessage = new String[]{"§4You are not Authentificated! You are in read-only mode!", "§6If you have an account, use rosepad and login with it", "§6If you don't have one, use /register or /login"};
        public String invalidPasswordMessage = "§cInvalid password!";
        public String alreadyLoggedIn = "§cYou are already logged in!";

        public static class JoinMessage {
            @TomlComment("Enable the join messages? Those messages will be sent on every join to the joining player")
            public boolean enable = true;
            @TomlComment({"Configure the message shown to players on join", "", "Available placeholders for the message lines are: ", "%player% - Player name", "%onlinecount% - Number of players online"})
            public String[] lines = new String[]{"§aWelcome %player%", "§aThere are §6%onlinecount%§a players online right now."};
        }
    }
}
