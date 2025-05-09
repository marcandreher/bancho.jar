package com.banchojar.commands;

import com.banchojar.Player;

public abstract class AbstractBanchoCommandHandler {

    /**
     * Command keyword, like "!help", "!roll", etc.
     */
    public abstract String commandName();

    /**
     * Short description of the command.
     */
    public abstract String description();

    /**
     * Handle the command logic.
     *
     * @param user The user who issued the command.
     * @param args The arguments passed with the command.
     * @return The response string to be sent back to the user.
     */
    public abstract String handle(Player player, String[] args);

    /**
     * Optional: Whether this command requires privileges (e.g., admin/mod).
     */
    public boolean requiresPrivileges() {
        return false;
    }

    /**
     * Optional: Minimum number of arguments.
     */
    public int minArgs() {
        return 0;
    }


}
