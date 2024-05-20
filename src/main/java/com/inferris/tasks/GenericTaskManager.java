package com.inferris.tasks;

import com.inferris.Inferris;
import net.md_5.bungee.api.scheduler.TaskScheduler;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

public class GenericTaskManager {
    private final TaskScheduler scheduler;
    private final Queue<DelayedTask> taskQueue;

    public GenericTaskManager(TaskScheduler scheduler) {
        this.scheduler = scheduler;
        this.taskQueue = new LinkedList<>();
    }

    public void addTask(Runnable task) {
        addTask(task, 0, TimeUnit.SECONDS);
    }

    public void addTask(Runnable task, long delay, TimeUnit unit) {
        DelayedTask delayedTask = new DelayedTask(task, delay, unit);
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
        scheduler.schedule(Inferris.getInstance(), () -> {
            nextTask.task().run();
            taskQueue.poll();
            processTasks();
        }, nextTask.delay(), nextTask.timeUnit());
    }

    private record DelayedTask(Runnable task, long delay, TimeUnit timeUnit) {
    }
}