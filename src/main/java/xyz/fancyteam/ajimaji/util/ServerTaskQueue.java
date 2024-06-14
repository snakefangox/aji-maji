package xyz.fancyteam.ajimaji.util;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

public class ServerTaskQueue {
    private static final Queue<Delayed> TASK_QUEUE = new LinkedBlockingDeque<>();

    private static class Delayed {
        int delay;
        final Runnable task;

        public Delayed(int delay, Runnable task) {
            this.delay = delay;
            this.task = task;
        }
    }

    public static void init() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            int taskCount = TASK_QUEUE.size();
            for (int i = 0; i < taskCount; i++) {
                Delayed delayed = TASK_QUEUE.poll();
                if (delayed != null) {
                    if (delayed.delay <= 1) {
                        delayed.task.run();
                    } else {
                        delayed.delay--;
                        TASK_QUEUE.offer(delayed);
                    }
                } else {
                    break;
                }
            }
        });
    }

    public static void submit(int delay, Runnable task) {
        TASK_QUEUE.offer(new Delayed(delay, task));
    }
}
