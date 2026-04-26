package me.diolezz.diolezspectate;

import me.diolezz.diolezspectate.commands.SpectateCommand;
import me.diolezz.diolezspectate.listeners.SpectateListener;
import me.diolezz.diolezspectate.managers.SpectateManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class DiolezSpectate extends JavaPlugin {

    private SpectateManager spectateManager;

    @Override
    public void onEnable() {
        printBranding();

        this.spectateManager = new SpectateManager(this);

        SpectateCommand spectateCommand = new SpectateCommand(this, spectateManager);
        getCommand("spectate").setExecutor(spectateCommand);
        getCommand("spectate").setTabCompleter(spectateCommand);

        getServer().getPluginManager().registerEvents(new SpectateListener(this, spectateManager), this);

        getLogger().info("DiolezSpectate has been enabled successfully.");
    }

    @Override
    public void onDisable() {
        if (spectateManager != null) {
            spectateManager.endAllSessions();
        }
        getLogger().info("DiolezSpectate has been disabled. All sessions cleaned up.");
    }

    public SpectateManager getSpectateManager() {
        return spectateManager;
    }

    private void printBranding() {
        String GOLD  = "\u001B[33m";
        String RESET = "\u001B[0m";

        String[] art = {
                " ",
                GOLD + "  ██████╗ ██╗ ██████╗ ██╗     ███████╗███████╗    " + RESET,
                GOLD + "  ██╔══██╗██║██╔═══██╗██║     ██╔════╝╚══███╔╝    " + RESET,
                GOLD + "  ██║  ██║██║██║   ██║██║     █████╗    ███╔╝     " + RESET,
                GOLD + "  ██║  ██║██║██║   ██║██║     ██╔══╝   ███╔╝      " + RESET,
                GOLD + "  ██████╔╝██║╚██████╔╝███████╗███████╗███████╗    " + RESET,
                GOLD + "  ╚═════╝ ╚═╝ ╚═════╝ ╚══════╝╚══════╝╚══════╝    " + RESET,
                " ",
                GOLD + "  ███████╗██████╗ ███████╗ ██████╗████████╗ █████╗ ████████╗███████╗" + RESET,
                GOLD + "  ██╔════╝██╔══██╗██╔════╝██╔════╝╚══██╔══╝██╔══██╗╚══██╔══╝██╔════╝" + RESET,
                GOLD + "  ███████╗██████╔╝█████╗  ██║        ██║   ███████║   ██║   █████╗  " + RESET,
                GOLD + "  ╚════██║██╔═══╝ ██╔══╝  ██║        ██║   ██╔══██║   ██║   ██╔══╝  " + RESET,
                GOLD + "  ███████║██║     ███████╗╚██████╗   ██║   ██║  ██║   ██║   ███████╗" + RESET,
                GOLD + "  ╚══════╝╚═╝     ╚══════╝ ╚═════╝   ╚═╝   ╚═╝  ╚═╝   ╚═╝   ╚══════╝" + RESET,
                " ",
                GOLD + "         [ DiolezSpectate ] - by Diolezz | Spigot 1.21" + RESET,
                " "
        };

        for (String line : art) {
            System.out.println(line);
        }
    }
}