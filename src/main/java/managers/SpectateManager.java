package me.diolezz.diolezspectate.managers;

import me.diolezz.diolezspectate.DiolezSpectate;
import me.diolezz.diolezspectate.model.SpectateSession;
import me.diolezz.diolezspectate.util.Messages;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import java.util.*;

public class SpectateManager {

    private final DiolezSpectate plugin;

    // spectatorId -> active session
    private final Map<UUID, SpectateSession> sessions = new HashMap<>();
    // targetId -> spectatorId (reverse lookup)
    private final Map<UUID, UUID> spectatorOf = new HashMap<>();
    // targetId -> spectatorId (pending requests)
    private final Map<UUID, UUID> pendingRequests = new HashMap<>();

    public SpectateManager(DiolezSpectate plugin) {
        this.plugin = plugin;
    }

    // --- Helpers ---

    public boolean isSpectating(UUID spectatorId) {
        return sessions.containsKey(spectatorId);
    }

    public boolean isBeingSpectated(UUID targetId) {
        return spectatorOf.containsKey(targetId);
    }

    public SpectateSession getSession(UUID spectatorId) {
        return sessions.get(spectatorId);
    }

    // New helper to fix RTP logic efficiently
    public UUID getSpectatorId(UUID targetId) {
        return spectatorOf.get(targetId);
    }

    // --- Request Logic ---

    public boolean hasPendingRequest(UUID targetId) {
        return pendingRequests.containsKey(targetId);
    }

    public UUID getPendingRequester(UUID targetId) {
        return pendingRequests.get(targetId);
    }

    public void addPendingRequest(UUID targetId, UUID spectatorId) {
        pendingRequests.put(targetId, spectatorId);
    }

    public void removePendingRequest(UUID targetId) {
        pendingRequests.remove(targetId);
    }

    public boolean hasOutgoingRequest(UUID spectatorId) {
        return pendingRequests.containsValue(spectatorId);
    }

    // --- Session Logic ---

    public void startSession(Player spectator, Player target) {
        UUID sid = spectator.getUniqueId();
        UUID tid = target.getUniqueId();

        // Save state
        SpectateSession session = new SpectateSession(sid, tid,
                spectator.getLocation(), spectator.getGameMode());

        sessions.put(sid, session);
        spectatorOf.put(tid, sid);

        // Set up spectator
        spectator.setGameMode(GameMode.SPECTATOR);
        spectator.teleport(target.getLocation());

        // Attach camera once (No repeating lock task)
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (spectator.isOnline() && target.isOnline()) {
                spectator.setSpectatorTarget(target);
            }
        }, 2L);

        spectator.sendMessage(Messages.prefix() + Messages.color(
                "&aYou are now spectating &e" + target.getName() + "&a. Use &f/spectate leave &ato stop."));
        target.sendMessage(Messages.prefix() + Messages.color(
                "&e" + spectator.getName() + " &ais now spectating you."));
    }

    public void endSession(UUID spectatorId, boolean announce) {
        SpectateSession session = sessions.remove(spectatorId);
        if (session == null) return;

        spectatorOf.remove(session.getTargetId());

        Player spectator = Bukkit.getPlayer(spectatorId);
        if (spectator != null && spectator.isOnline()) {
            spectator.setSpectatorTarget(null);
            spectator.setGameMode(session.getOriginalGameMode());
            spectator.teleport(session.getOriginalLocation());
            if (announce) {
                spectator.sendMessage(Messages.prefix() + Messages.color(
                        "&cYour spectate session has ended."));
            }
        }

        Player target = Bukkit.getPlayer(session.getTargetId());
        if (target != null && target.isOnline() && announce) {
            target.sendMessage(Messages.prefix() + Messages.color(
                    "&e" + (spectator != null ? spectator.getName() : "A player") + " &chas stopped spectating you."));
        }
    }

    public void handleTargetDisconnect(UUID targetId) {
        UUID spectatorId = spectatorOf.get(targetId);
        if (spectatorId == null) return;

        Player spectator = Bukkit.getPlayer(spectatorId);
        if (spectator != null) {
            spectator.sendMessage(Messages.prefix() + Messages.color(
                    "&cThe target disconnected."));
        }
        endSession(spectatorId, false);
    }

    public void handleSpectatorDisconnect(UUID spectatorId) {
        SpectateSession session = sessions.remove(spectatorId);
        if (session == null) return;

        spectatorOf.remove(session.getTargetId());

        Player target = Bukkit.getPlayer(session.getTargetId());
        if (target != null && target.isOnline()) {
            target.sendMessage(Messages.prefix() + Messages.color(
                    "&cYour spectator has disconnected."));
        }
    }

    public void endAllSessions() {
        new HashSet<>(sessions.keySet()).forEach(id -> endSession(id, true));
        pendingRequests.clear();
    }
}