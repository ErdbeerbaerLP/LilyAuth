package de.erdbeerbaerlp.lilyauth;

import de.erdbeerbaerlp.lilyauth.config.LilyAuthConfig;
import net.minecraft.src.EntityPlayerMP;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.player.*;
import ru.vladthemountain.lilybukkit.core.entity.LBPlayer;

import java.lang.reflect.Field;
import java.net.Socket;

public class AuthPlayerListener extends PlayerListener {

    public void onPlayerChat(final PlayerChatEvent ev) {
        if (!LilyAuth.isPlayerAuthed(ev.getPlayer())) {
            ev.setCancelled(true);
            for (final String s : LilyAuthConfig.instance().messages.cannotChatMessage) {
                ev.getPlayer().sendMessage(s);
            }
        }
    }

    public void onPlayerCommandPreprocess(final PlayerCommandPreprocessEvent event) {
        if (!event.getMessage().startsWith("/register") && !event.getMessage().startsWith("/login")) {
            this.cancelIfNotAuthed(event, event.getPlayer());
        }
    }

    public void onPlayerJoin(final PlayerJoinEvent event) {
        LilyAuth.loginStatus.put(UUIDUtils.getUUIDFromPlayer(event.getPlayer()), false);
        if (LilyAuthConfig.instance().messages.joinMessage.enable)
            for (String s : LilyAuthConfig.instance().messages.joinMessage.lines) {
                event.getPlayer().sendMessage(s.replace("%player%", event.getPlayer().getDisplayName()).replace("%onlinecount%", Bukkit.getOnlinePlayers().length + ""));
            }
        if (LilyAuthConfig.instance().savePlayerIPs)
            try {
                final Field entity = ((LBPlayer) event.getPlayer()).getClass().getDeclaredField("entity");
                entity.setAccessible(true);
                final EntityPlayerMP o = (EntityPlayerMP) entity.get(event.getPlayer());
                final Field socket = o.playerNetServerHandler.netManager.getClass().getDeclaredField("networkSocket");
                socket.setAccessible(true);
                final Socket sock = (Socket) socket.get(o.playerNetServerHandler.netManager);
                if (LilyAuth.checkLastKnownIP(UUIDUtils.getUUIDFromName(event.getPlayer().getName()), sock.getInetAddress().getHostAddress()))
                {
                    LilyAuth.loginStatus.put(UUIDUtils.getUUIDFromPlayer(event.getPlayer()), true);
                    event.getPlayer().sendMessage(LilyAuthConfig.instance().messages.loggedInWithIP);
                }
            } catch (Exception ignored) {
            }

        if (!LilyAuth.isPlayerAuthed(event.getPlayer())) {
            event.getPlayer().sendMessage(" ");
            for (String s : LilyAuthConfig.instance().messages.notAuthentificatedJoinMessage) {
                event.getPlayer().sendMessage(s);
            }
        }
    }

    public void onPlayerDropItem(final PlayerDropItemEvent event) {
        this.cancelIfNotAuthed(event, event.getPlayer());
    }

    public void onPlayerPickupItem(final PlayerPickupItemEvent event) {
        this.cancelIfNotAuthed(event, event.getPlayer());
    }

    private void cancelIfNotAuthed(final Cancellable ev, final Player p) {
        if (!LilyAuth.isPlayerAuthed(p)) {
            ev.setCancelled(true);
        }
    }

    public void onPlayerInteract(final PlayerInteractEvent event) {
        this.cancelIfNotAuthed(event, event.getPlayer());
    }

    public void onPlayerInteractEntity(final PlayerInteractEntityEvent event) {
        this.cancelIfNotAuthed(event, event.getPlayer());
    }

    public void onPlayerBucketFill(final PlayerBucketFillEvent event) {
        this.cancelIfNotAuthed(event, event.getPlayer());
    }

    public void onPlayerBucketEmpty(final PlayerBucketEmptyEvent event) {
        this.cancelIfNotAuthed(event, event.getPlayer());
    }

    public void onPlayerQuit(final PlayerQuitEvent event) {
        LilyAuth.loginStatus.remove(UUIDUtils.getUUIDFromPlayer(event.getPlayer()));
    }

    public void onPlayerPreLogin(final PlayerPreLoginEvent event) {

    }
}
