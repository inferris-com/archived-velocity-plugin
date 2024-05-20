package com.inferris.tasks;

import com.inferris.Inferris;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.scheduler.TaskScheduler;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class PlayerTaskManager {
    private final TaskScheduler scheduler;
    private final Queue<DelayedTask> taskQueue;

    public PlayerTaskManager(TaskScheduler scheduler) {
        this.scheduler = scheduler;
        this.taskQueue = new LinkedList<>();
    }

    public void addTaskForPlayer(ProxiedPlayer player, Runnable task) {
        addTaskForPlayer(player, task, 0, TimeUnit.SECONDS);
    }

    public void addTaskForPlayer(ProxiedPlayer player, Runnable task, long delay, TimeUnit unit) {
        DelayedTask delayedTask = new DelayedTask(player, task, delay, unit);
        taskQueue.offer(delayedTask);
        if (taskQueue.size() == 1) {
            processTasks();
        }
    }

    private void processTasks() {
        if (taskQueue.isEmpty()) {
            return;
        }

        DelayedTask nextTask = taskQueue.peek();
        if (nextTask.player() == null || nextTask.player().isConnected()) {
            scheduler.schedule(Inferris.getInstance(), () -> {
                if (nextTask.player() != null && nextTask.player().isConnected()) {
                    nextTask.task().run();
                }
                taskQueue.poll();
                processTasks();
            }, nextTask.delay(), nextTask.timeUnit());
        } else {
            taskQueue.poll();
            processTasks();
        }
    }

    private record DelayedTask(ProxiedPlayer player, Runnable task, long delay, TimeUnit timeUnit) {
    }
}