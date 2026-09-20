package com.xtremerpie.ascension.achievements;

public final class AchievementProgress {

    private final String achievementId;
    private double currentValue;
    private boolean completed;
    private long completedAtEpochMillis = -1;
    private int timesCompleted = 0;

    public AchievementProgress(String achievementId) {
        this.achievementId = achievementId;
    }

    public String achievementId() {
        return achievementId;
    }

    public double currentValue() {
        return currentValue;
    }

    public void setCurrentValue(double value) {
        this.currentValue = value;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void markCompleted() {
        this.completed = true;
        this.completedAtEpochMillis = System.currentTimeMillis();
        this.timesCompleted++;
    }

    /** Resets completion (only meaningful for repeatable achievements). */
    public void resetForRepeat() {
        this.completed = false;
    }

    public long completedAtEpochMillis() {
        return completedAtEpochMillis;
    }

    public int timesCompleted() {
        return timesCompleted;
    }
}
