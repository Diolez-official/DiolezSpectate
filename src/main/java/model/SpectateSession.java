package me.diolezz.diolezspectate.model;

import org.bukkit.GameMode;
import org.bukkit.Location;

import java.util.UUID;

/**
 * Holds all data for an active spectating session.
 * Stored in SpectateManager's HashMap, cleaned up on session end.
 */
public class SpectateSession {

    private final UUID spectatorId;
    private final UUID targetId;
    private final Location originalLocation;
    private final GameMode originalGameMode;

    public SpectateSession(UUID spectatorId, UUID targetId, Location originalLocation, GameMode originalGameMode) {
        this.spectatorId = spectatorId;
        this.targetId = targetId;
        this.originalLocation = originalLocation.clone(); // clone to avoid reference mutation
        this.originalGameMode = originalGameMode;
    }

    public UUID getSpectatorId() { return spectatorId; }
    public UUID getTargetId()    { return targetId; }
    public Location getOriginalLocation() { return originalLocation.clone(); }
    public GameMode getOriginalGameMode() { return originalGameMode; }
}