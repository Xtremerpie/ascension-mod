package com.xtremerpie.ascension.notification;

import com.xtremerpie.ascension.config.AscensionConfig;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Client-side notification queue. Bounded to {@link #MAX_VISIBLE} stacked
 * entries so spam (e.g. many achievements completing in one tick from a
 * batch reward) can't flood the screen — extras simply wait their turn.
 */
public final class NotificationManager {

    private static final NotificationManager CLIENT_INSTANCE = new NotificationManager();

    public static NotificationManager client() {
        return CLIENT_INSTANCE;
    }

    private static final int MAX_VISIBLE = 4;
    private static final int MAX_QUEUED = 20;

    private final Deque<Notification> queue = new ArrayDeque<>();
    private int currentTick = 0;

    public void tick() {
        currentTick++;
        queue.removeIf(n -> n.isExpired(currentTick));
    }

    public void push(Notification.Category category, String title, String subtitle, String detail) {
        if (!AscensionConfig.get().notificationsEnabled) return;
        if (queue.size() >= MAX_QUEUED) {
            queue.pollFirst(); // drop oldest rather than grow unbounded
        }
        queue.addLast(new Notification(category, title, subtitle, detail, currentTick,
                AscensionConfig.get().notificationDurationTicks));
    }

    public List<Notification> visible() {
        return queue.stream().limit(MAX_VISIBLE).collect(Collectors.toList());
    }

    public List<Notification> visibleOf(Notification.Category category) {
        return queue.stream()
                .filter(n -> n.category() == category)
                .limit(MAX_VISIBLE)
                .collect(Collectors.toList());
    }

    public int currentTick() {
        return currentTick;
    }
}
