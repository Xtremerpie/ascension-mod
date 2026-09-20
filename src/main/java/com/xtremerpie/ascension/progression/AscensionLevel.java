package com.xtremerpie.ascension.progression;

/**
 * Pure XP-curve math. Levels 1..20 (spec section 22 example goes to Level
 * 20 / Transcendent). Curve: XP required for level N = 200 * N * (N+1)/2
 * roughly — i.e. quadratically increasing, gentle at low levels, so early
 * milestones (Level 5 unlocking advanced HUD) come reasonably fast.
 */
public final class AscensionLevel {

    private AscensionLevel() {
    }

    public static final int MAX_LEVEL = 20;

    /** Total cumulative XP needed to REACH this level from zero. */
    public static double xpRequiredForLevel(int level) {
        if (level <= 0) return 0;
        return 200.0 * level * (level + 1) / 2.0;
    }

    public static int levelForXp(double totalXp) {
        int level = 0;
        while (level < MAX_LEVEL && totalXp >= xpRequiredForLevel(level + 1)) {
            level++;
        }
        return level;
    }

    public static double xpIntoCurrentLevel(double totalXp) {
        int level = levelForXp(totalXp);
        return totalXp - xpRequiredForLevel(level);
    }

    public static double xpNeededForNextLevel(double totalXp) {
        int level = levelForXp(totalXp);
        if (level >= MAX_LEVEL) return 0;
        return xpRequiredForLevel(level + 1) - xpRequiredForLevel(level);
    }

    public static double progressFraction(double totalXp) {
        int level = levelForXp(totalXp);
        if (level >= MAX_LEVEL) return 1.0;
        double needed = xpNeededForNextLevel(totalXp);
        return needed <= 0 ? 1.0 : xpIntoCurrentLevel(totalXp) / needed;
    }

    /** Milestone unlocks, per spec section 22. Extend this table for new milestones. */
    public static String milestoneUnlockedAt(int level) {
        return switch (level) {
            case 5 -> "Advanced HUD";
            case 10 -> "Structure Blueprints";
            case 15 -> "Advanced Physics Visualization";
            case 20 -> "Ascendant Status";
            default -> null;
        };
    }
}
