package de.erdbeerbaerlp.lilyauth.command;

import de.erdbeerbaerlp.lilyauth.LilyAuth;
import de.erdbeerbaerlp.lilyauth.UUIDUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.security.NoSuchAlgorithmException;

public class RegisterCommand implements CommandExecutor {
    private final LilyAuth plugin;

    public RegisterCommand(final LilyAuth plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(final CommandSender commandSender, final Command command, final String s, final String[] strings) {
        if (strings.length < 1) {
            commandSender.sendMessage("\u00c2§7Use /register <unique password> to register");
            commandSender.sendMessage("\u00c2§7To login later, use /login <password>");
        } else {
            final String pass = strings[0];
            if (LilyAuth.userExists(UUIDUtils.getUUIDFromName(commandSender.getName()))) {
                commandSender.sendMessage("\u00c2§cYou are already registered!");
            } else {
                commandSender.sendMessage("\u00c2§aRegistered successfully! You have been logged in!");
                try {
                    LilyAuth.register(UUIDUtils.getUUIDFromName(commandSender.getName()), pass);
                } catch (NoSuchAlgorithmException ignored) {
                }
                LilyAuth.loginStatus.put(UUIDUtils.getUUIDFromName(commandSender.getName()), true);
            }
        }
        return true;
    }
}
