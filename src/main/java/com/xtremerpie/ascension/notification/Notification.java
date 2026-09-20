package com.xtremerpie.ascension.notification;

/**
 * A single stackable HUD notification. Expiry is tick-based (not
 * wall-clock) so it stays in lockstep with the client tick loop that
 * drives everything else in the mod.
 */
public record Notification(
        Category category,
        String title,
        String subtitle,
        String detail,
        int createdAtTick,
        int durationTicks
) {
    public enum Category {
        ACHIEVEMENT, PROGRESSION, BLUEPRINT, STRUCTURE, GENERAL
    }

    public boolean isExpired(int currentTick) {
        return (currentTick - createdAtTick) >= durationTicks;
    }
}
