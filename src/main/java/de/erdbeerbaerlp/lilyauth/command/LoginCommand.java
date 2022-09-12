package de.erdbeerbaerlp.lilyauth.command;

import de.erdbeerbaerlp.lilyauth.LilyAuth;
import de.erdbeerbaerlp.lilyauth.UUIDUtils;
import de.erdbeerbaerlp.lilyauth.config.LilyAuthConfig;
import net.minecraft.server.MinecraftServer;
import net.minecraft.src.EntityPlayerMP;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.vladthemountain.lilybukkit.core.entity.LBPlayer;

import java.lang.reflect.Field;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public class LoginCommand implements CommandExecutor {
    private final LilyAuth plugin;

    public LoginCommand(final LilyAuth plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(final CommandSender commandSender, final Command command, final String s, final String[] strings) {
        final UUID uuid = UUIDUtils.getUUIDFromName(commandSender.getName());
        if (strings.length < 1) {
            commandSender.sendMessage(LilyAuthConfig.instance().messages.noPassword);
        } else {
            final String pass = strings[0];
            final Player player = Bukkit.getPlayer(commandSender.getName());
            try {
                if (LilyAuth.checkPassword(uuid, pass)) {
                    LilyAuth.loginStatus.put(uuid, true);
                    commandSender.sendMessage(LilyAuthConfig.instance().messages.loggedIn);
                    if (LilyAuthConfig.instance().savePlayerIPs){
                        try {
                            final Field entity = ((LBPlayer) player).getClass().getDeclaredField("entity");
                            entity.setAccessible(true);
                            final EntityPlayerMP o = (EntityPlayerMP) entity.get(player);
                            final Field socket = o.playerNetServerHandler.netManager.getClass().getDeclaredField("networkSocket");
                            socket.setAccessible(true);
                            final Socket sock = (Socket) socket.get(o.playerNetServerHandler.netManager);
                            LilyAuth.setLastKnownIP(uuid, sock.getInetAddress().getHostAddress());
                        }catch (Exception ignored) {

                        }
                    }
                } else {
                    commandSender.sendMessage(LilyAuthConfig.instance().messages.invalidPasswordMessage);
                    LilyAuth.appendFailedAttempt(uuid);
                    if (LilyAuth.failedAttempts.getOrDefault(uuid, 0) > 10) {
                        Bukkit.getLogger().info("Bruteforce-Banning " + player.getName());
                        try {
                            final Field entity = ((LBPlayer) player).getClass().getDeclaredField("entity");
                            entity.setAccessible(true);
                            final EntityPlayerMP o = (EntityPlayerMP) entity.get(player);
                            final Field socket = o.playerNetServerHandler.netManager.getClass().getDeclaredField("networkSocket");
                            socket.setAccessible(true);
                            final Socket sock = (Socket) socket.get(o.playerNetServerHandler.netManager);
                            MinecraftServer.INSTANCE.configManager.banIP(sock.getInetAddress().getHostAddress());
                        } catch (NoSuchFieldException | IllegalAccessException ex) {
                            Bukkit.getLogger().warning("IP Ban failed");
                            ex.printStackTrace();
                        }
                        MinecraftServer.INSTANCE.configManager.banPlayer(player.getName());
                        LilyAuth.unregister(uuid);
                        player.kickPlayer(LilyAuthConfig.instance().messages.bruteforceBanMessage);
                    }
                }
            } catch (NoSuchAlgorithmException ignored) {
            }
        }
        return true;
    }
}
