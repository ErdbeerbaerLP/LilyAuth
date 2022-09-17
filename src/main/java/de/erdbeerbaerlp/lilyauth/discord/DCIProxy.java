package de.erdbeerbaerlp.lilyauth.discord;

import de.erdbeerbaerlp.dcintegration.common.storage.CommandRegistry;

/**
 * Class that only gets called to load when Discord Integration exists
 */
public class DCIProxy {

    public static void registerCommands() {
        CommandRegistry.registerCommand(new PasswordCommand());
    }
}
