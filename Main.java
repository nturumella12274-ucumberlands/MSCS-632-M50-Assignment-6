import java.util.*;
import java.util.concurrent.locks.*;

// A simple task queue with explicit locking.
class TaskQueue {
    private final Queue<Integer> queue = new LinkedList<>();
    private final Lock lock = new ReentrantLock();

    public void addTask(int task) {
        lock.lock();
        try {
            queue.add(task);
        } finally {
            lock.unlock();
        }
    }

    public Integer getTask() {
        lock.lock();
        try {
            return queue.poll();
        } finally {
            lock.unlock();
        }
    }

    public boolean isEmpty() {
        lock.lock();
        try {
            return queue.isEmpty();
        } finally {
            lock.unlock();
        }
    }
}

// Worker which processes tasks from the TaskQueue.
class Worker implements Runnable {
    private final int id;
    private final TaskQueue queue;
    private final List<String> results;

    public Worker(int id, TaskQueue queue, List<String> results) {
        this.id = id;
        this.queue = queue;
        this.results = results;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Integer task = queue.getTask();
                if (task == null)
                    break;

                System.out.println("Worker " + id + " started task " + task);
                Thread.sleep(1000); // Simulate processing delay

                String result = "Worker " + id + " completed task " + task;
                synchronized (results) {
                    results.add(result);
                }
                System.out.println(result);
            }
        } catch (InterruptedException e) {
            System.err.println("Worker " + id + " was interrupted.");
        } catch (Exception e) {
            System.err.println("Worker " + id + " encountered an error: " + e.getMessage());
        }
    }
}

public class Main {
    public static void main(String[] args) {
        TaskQueue queue = new TaskQueue();
        List<String> results = Collections.synchronizedList(new ArrayList<>());

        // Add 20 tasks
        for (int i = 1; i <= 20; i++) {
            queue.addTask(i);
        }

        // Start 5 worker threads
        Thread[] workers = new Thread[5];
        for (int i = 0; i < 5; i++) {
            workers[i] = new Thread(new Worker(i + 1, queue, results));
            workers[i].start();
        }

        // Wait for all worker threads to complete
        for (Thread worker : workers) {
            try {
                worker.join();
            } catch (InterruptedException e) {
                System.err.println("Error waiting for threads to finish: " + e.getMessage());
            }
        }

        // Output results to the console.
        System.out.println("\nResults of tasks:");
        for (String result : results) {
            System.out.println(result);
        }

        System.out.println("All tasks completed.");
    }
}
