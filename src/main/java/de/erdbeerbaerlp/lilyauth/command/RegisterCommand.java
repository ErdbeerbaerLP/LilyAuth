package de.erdbeerbaerlp.lilyauth.command;

import de.erdbeerbaerlp.lilyauth.LilyAuth;
import de.erdbeerbaerlp.lilyauth.UUIDUtils;
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

public class RegisterCommand implements CommandExecutor {

    public boolean onCommand(final CommandSender commandSender, final Command command, final String s, final String[] strings) {
        UUID uuid = UUIDUtils.getUUIDFromName(commandSender.getName());
        if (strings.length < 1) {
            commandSender.sendMessage("\u00c2§7Use /register <unique password> to register");
            commandSender.sendMessage("\u00c2§7To login later, use /login <password>");
        } else {
            final String pass = strings[0];
            if (LilyAuth.userExists(uuid)) {
                commandSender.sendMessage("\u00c2§cYou are already registered!");
            } else {
                commandSender.sendMessage("\u00c2§aRegistered successfully! You have been logged in!");
                try {
                    LilyAuth.register(uuid, pass);
                } catch (NoSuchAlgorithmException ignored) {
                }
                LilyAuth.loginStatus.put(uuid, true);

                try {
                    final Player player = Bukkit.getPlayer(commandSender.getName());
                    final Field entity = ((LBPlayer) player).getClass().getDeclaredField("entity");
                    entity.setAccessible(true);
                    final EntityPlayerMP o = (EntityPlayerMP) entity.get(player);
                    final Field socket = o.playerNetServerHandler.netManager.getClass().getDeclaredField("networkSocket");
                    socket.setAccessible(true);
                    final Socket sock = (Socket) socket.get(o.playerNetServerHandler.netManager);
                    LilyAuth.setLastKnownIP(uuid, sock.getInetAddress().getHostAddress());
                } catch (Exception ignored) {

                }
            }
        }
        return true;
    }
}
