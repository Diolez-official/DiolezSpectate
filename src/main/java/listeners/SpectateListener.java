package me.diolezz.diolezspectate.listeners;

import me.diolezz.diolezspectate.DiolezSpectate;
import me.diolezz.diolezspectate.managers.SpectateManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class SpectateListener implements Listener {

    private final DiolezSpectate plugin;
    private final SpectateManager manager;

    public SpectateListener(DiolezSpectate plugin, SpectateManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    // 1. Handle Target Teleportation (RTP, Pearls, Warps)
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTargetTeleport(PlayerTeleportEvent event) {
        Player target = event.getPlayer();

        // Check if this player is being spectated
        if (manager.isBeingSpectated(target.getUniqueId())) {

            UUID spectatorId = manager.getSpectatorId(target.getUniqueId());
            Player spectator = Bukkit.getPlayer(spectatorId);

            if (spectator != null && spectator.isOnline()) {
                // IMPORTANT: When target TPs, we must manually TP the spectator
                // because vanilla Spectator mode often detaches on long-distance TPs.

                // We teleport the spectator to the destination immediately
                spectator.teleport(event.getTo());

                // Re-attach the camera 1 tick later to ensure client processes the chunk/TP
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (spectator.isOnline() && target.isOnline()) {
                            spectator.setSpectatorTarget(target);
                        }
                    }
                }.runTaskLater(plugin, 1L);
            }
        }
    }

    // 2. Prevent Spectator from teleporting themselves (Roaming)
    @EventHandler
    public void onSpectatorTeleport(PlayerTeleportEvent event) {
        Player spectator = event.getPlayer();

        if (!manager.isSpectating(spectator.getUniqueId())) return;

        PlayerTeleportEvent.TeleportCause cause = event.getCause();

        // Allow Plugin/Command teleports (like the one we just did in onTargetTeleport)
        if (cause == PlayerTeleportEvent.TeleportCause.PLUGIN || cause == PlayerTeleportEvent.TeleportCause.COMMAND) {
            return;
        }

        // Also allow SPECTATE cause (Vanilla movement), but if they try to use /tp or enderpearls, block it.
        // If we want to strictly prevent roaming "outside" the player, we usually block UNKNOWN or SPECTATE.
        // However, since we removed the lock system, letting them drift slightly is fine,
        // but we block major teleports.
        if (cause == PlayerTeleportEvent.TeleportCause.ENDER_PEARL || cause == PlayerTeleportEvent.TeleportCause.CHORUS_FRUIT) {
            event.setCancelled(true);
        }
    }

    // 3. Handle Disconnects
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();

        if (manager.isSpectating(uuid)) {
            manager.handleSpectatorDisconnect(uuid);
        }

        if (manager.isBeingSpectated(uuid)) {
            manager.handleTargetDisconnect(uuid);
        }

        if (manager.hasPendingRequest(uuid)) {
            manager.removePendingRequest(uuid);
        }
    }
}