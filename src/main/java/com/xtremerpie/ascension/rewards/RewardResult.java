package com.xtremerpie.ascension.rewards;

public record RewardResult(boolean success, String summary) {
    public static RewardResult ok(String summary) {
        return new RewardResult(true, summary);
    }

    public static RewardResult failed(String reason) {
        return new RewardResult(false, reason);
    }
}
