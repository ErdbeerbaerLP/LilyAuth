package de.erdbeerbaerlp.lilyauth.discord;

import de.erdbeerbaerlp.dcintegration.common.discordCommands.DiscordCommand;
import de.erdbeerbaerlp.dcintegration.common.storage.CommandRegistry;
import de.erdbeerbaerlp.dcintegration.common.storage.PlayerLinkController;
import de.erdbeerbaerlp.lilyauth.LilyAuth;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;

import java.security.NoSuchAlgorithmException;
import java.util.concurrent.CompletableFuture;

public class PasswordCommand extends DiscordCommand {

    public PasswordCommand() {
        super("passwordreset", "Resets your ingame login password");
        this.addOption(OptionType.STRING, "password", "Password to set", true);
    }

    public void execute(final SlashCommandInteractionEvent ev, final ReplyCallbackAction replyCallbackAction) {
        final OptionMapping password = ev.getOption("password");
        final CompletableFuture<InteractionHook> reply = replyCallbackAction.setEphemeral(true).submit();
        if (PlayerLinkController.isDiscordLinked(ev.getUser().getId())) {
            if (password != null) {
                try {
                    LilyAuth.register(PlayerLinkController.getPlayerFromDiscord(ev.getUser().getId()), password.getAsString());
                } catch (NoSuchAlgorithmException ignored) {
                }
                reply.thenAccept(c -> c.editOriginal("Password reset").queue());
            } else {
                System.out.println("password == null");
            }
        } else {
            reply.thenAccept(c -> c.editOriginal("You can't reset your password").queue());
        }
    }

    public boolean canUserExecuteCommand(final User user) {
        return true;
    }
}
