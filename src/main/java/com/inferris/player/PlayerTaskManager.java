package com.inferris.player;

import com.inferris.Inferris;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.scheduler.TaskScheduler;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

public class PlayerTaskManager {
    private final TaskScheduler scheduler;
    private final Queue<DelayedTask> taskQueue;

    public PlayerTaskManager(TaskScheduler scheduler) {
        this.scheduler = scheduler;
        this.taskQueue = new LinkedList<>();
    }

    public void addTaskForPlayer(Runnable task) {
        taskQueue.offer(new DelayedTask(task, 0, TimeUnit.SECONDS));
        if (taskQueue.size() == 1) {
            // If this is the first task in the queue, start processing tasks
            processTasks();
        }
    }

    public void addTaskForPlayer(Runnable task, long delay, TimeUnit unit) {
        taskQueue.offer(new DelayedTask(task, delay, unit));
        if (taskQueue.size() == 1) {
            // If this is the first task in the queue, start processing tasks
            processTasks();
        }
    }

    private void processTasks() {
        if (taskQueue.isEmpty()) {
            return; // No more tasks to process
        }

        // Retrieve and execute the next task in the queue
        DelayedTask nextTask = taskQueue.peek();
        scheduler.schedule(Inferris.getInstance(), () -> {
            nextTask.getTask().run();
            // After the task is finished, remove it from the queue and process the next task
            taskQueue.poll();
            processTasks();
        }, nextTask.getDelay(), nextTask.getTimeUnit());
    }

    private static class DelayedTask {
        private final Runnable task;
        private final long delay;
        private final TimeUnit timeUnit;

        public DelayedTask(Runnable task, long delay, TimeUnit timeUnit) {
            this.task = task;
            this.delay = delay;
            this.timeUnit = timeUnit;
        }

        public Runnable getTask() {
            return task;
        }

        public long getDelay() {
            return delay;
        }

        public TimeUnit getTimeUnit() {
            return timeUnit;
        }
    }
}

