package me.diolezz.diolezspectate.commands;

import me.diolezz.diolezspectate.DiolezSpectate;
import me.diolezz.diolezspectate.managers.SpectateManager;
import me.diolezz.diolezspectate.util.Messages;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Handles all /spectate subcommands:
 *   /spectate request <player>
 *   /spectate accept
 *   /spectate deny
 *   /spectate leave
 */
public class SpectateCommand implements CommandExecutor, TabCompleter {

    private final DiolezSpectate plugin;
    private final SpectateManager manager;

    private static final String PERM = "diolezspectate.use";

    public SpectateCommand(DiolezSpectate plugin, SpectateManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Messages.color("&cOnly players can use this command."));
            return true;
        }

        if (!player.hasPermission(PERM)) {
            player.sendMessage(Messages.prefix() + Messages.color("&cYou don't have permission to use this command."));
            return true;
        }

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "request" -> handleRequest(player, args);
            case "accept"  -> handleAccept(player);
            case "deny"    -> handleDeny(player);
            case "leave"   -> handleLeave(player);
            default        -> sendHelp(player);
        }

        return true;
    }

    // -----------------------------------------------------------------------

    private void handleRequest(Player requester, String[] args) {
        if (args.length < 2) {
            requester.sendMessage(Messages.prefix() + Messages.color("&cUsage: /spectate request <player>"));
            return;
        }

        if (manager.isSpectating(requester.getUniqueId())) {
            requester.sendMessage(Messages.prefix() + Messages.color("&cYou are already spectating someone. Use &f/spectate leave &cfirst."));
            return;
        }

        if (manager.hasOutgoingRequest(requester.getUniqueId())) {
            requester.sendMessage(Messages.prefix() + Messages.color("&cYou already have a pending request. Wait for a response."));
            return;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            requester.sendMessage(Messages.prefix() + Messages.color("&cPlayer &f" + args[1] + " &cis not online."));
            return;
        }

        if (target.equals(requester)) {
            requester.sendMessage(Messages.prefix() + Messages.color("&cYou cannot spectate yourself."));
            return;
        }

        if (manager.hasPendingRequest(target.getUniqueId())) {
            requester.sendMessage(Messages.prefix() + Messages.color("&c" + target.getName() + " already has a pending spectate request."));
            return;
        }

        if (manager.isBeingSpectated(target.getUniqueId())) {
            requester.sendMessage(Messages.prefix() + Messages.color("&c" + target.getName() + " is already being spectated."));
            return;
        }

        // Store request
        manager.addPendingRequest(target.getUniqueId(), requester.getUniqueId());

        // Notify requester
        requester.sendMessage(Messages.prefix() + Messages.color(
                "&aSpectate request sent to &e" + target.getName() + "&a. Waiting for response..."));

        // Send clickable message to target
        sendRequestNotification(target, requester);
    }

    private void sendRequestNotification(Player target, Player requester) {
        target.sendMessage(Messages.color("&8&m------------------------------------------"));
        target.sendMessage(Messages.color("   &6[DiolezSpectate] &eSpectate Request"));
        target.sendMessage(Messages.color("   &7Player &f" + requester.getName() + " &7wants to spectate you."));
        target.sendMessage("");

        // Accept button
        TextComponent acceptBtn = new TextComponent(Messages.color("        &a&l[ ACCEPT ]"));
        acceptBtn.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/spectate accept"));
        acceptBtn.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder(Messages.color("&aClick to accept the spectate request.")).create()));

        // Spacer
        TextComponent spacer = new TextComponent("   ");

        // Deny button
        TextComponent denyBtn = new TextComponent(Messages.color("&c&l[ DENY ]"));
        denyBtn.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/spectate deny"));
        denyBtn.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder(Messages.color("&cClick to deny the spectate request.")).create()));

        target.spigot().sendMessage(acceptBtn, spacer, denyBtn);
        target.sendMessage("");
        target.sendMessage(Messages.color("&8&m------------------------------------------"));
    }

    // -----------------------------------------------------------------------

    private void handleAccept(Player target) {
        UUID tid = target.getUniqueId();

        if (!manager.hasPendingRequest(tid)) {
            target.sendMessage(Messages.prefix() + Messages.color("&cYou have no pending spectate request."));
            return;
        }

        UUID spectatorId = manager.getPendingRequester(tid);
        manager.removePendingRequest(tid);

        Player spectator = Bukkit.getPlayer(spectatorId);
        if (spectator == null || !spectator.isOnline()) {
            target.sendMessage(Messages.prefix() + Messages.color("&cThe requester has gone offline."));
            return;
        }

        manager.startSession(spectator, target);
    }

    // -----------------------------------------------------------------------

    private void handleDeny(Player target) {
        UUID tid = target.getUniqueId();

        if (!manager.hasPendingRequest(tid)) {
            target.sendMessage(Messages.prefix() + Messages.color("&cYou have no pending spectate request."));
            return;
        }

        UUID spectatorId = manager.getPendingRequester(tid);
        manager.removePendingRequest(tid);

        target.sendMessage(Messages.prefix() + Messages.color("&cYou denied the spectate request."));

        Player spectator = Bukkit.getPlayer(spectatorId);
        if (spectator != null && spectator.isOnline()) {
            spectator.sendMessage(Messages.prefix() + Messages.color(
                    "&e" + target.getName() + " &cdenied your spectate request."));
        }
    }

    // -----------------------------------------------------------------------

    private void handleLeave(Player spectator) {
        if (!manager.isSpectating(spectator.getUniqueId())) {
            spectator.sendMessage(Messages.prefix() + Messages.color("&cYou are not currently spectating anyone."));
            return;
        }

        manager.endSession(spectator.getUniqueId(), true);
    }

    // -----------------------------------------------------------------------

    private void sendHelp(Player player) {
        player.sendMessage(Messages.color("&8&m------------------------------------------"));
        player.sendMessage(Messages.color("   &6[DiolezSpectate] &7Commands"));
        player.sendMessage(Messages.color("   &f/spectate request <player> &7- Request to spectate"));
        player.sendMessage(Messages.color("   &f/spectate accept            &7- Accept a request"));
        player.sendMessage(Messages.color("   &f/spectate deny              &7- Deny a request"));
        player.sendMessage(Messages.color("   &f/spectate leave             &7- Stop spectating"));
        player.sendMessage(Messages.color("&8&m------------------------------------------"));
    }

    // -----------------------------------------------------------------------
    //  Tab completion
    // -----------------------------------------------------------------------

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player player) || !player.hasPermission(PERM)) return List.of();

        if (args.length == 1) {
            return Arrays.asList("request", "accept", "deny", "leave")
                    .stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("request")) {
            return Bukkit.getOnlinePlayers().stream()
                    .filter(p -> !p.equals(player))
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return List.of();
    }
}