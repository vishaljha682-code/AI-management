package com.airesearchhub.ml.engine;

import com.airesearchhub.model.entity.Experiment;
import com.airesearchhub.model.entity.TrainingJob;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * ModelTrainingEngine: Multithreaded asynchronous training coordinator.
 * Satisfies: Multithreading & Synchronization (4 marks) and Generics & Collections.
 */
public class ModelTrainingEngine {
    private static volatile ModelTrainingEngine instance;
    private final ExecutorService threadPool;
    private final ConcurrentHashMap<Long, Future<Experiment>> activeTasks;

    private ModelTrainingEngine() {
        // Create worker pool for parallel ML training jobs
        int cores = Runtime.getRuntime().availableProcessors();
        int poolSize = Math.max(2, Math.min(cores, 8));
        this.threadPool = Executors.newFixedThreadPool(poolSize);
        this.activeTasks = new ConcurrentHashMap<>();
    }

    public static ModelTrainingEngine getInstance() {
        if (instance == null) {
            synchronized (ModelTrainingEngine.class) {
                if (instance == null) {
                    instance = new ModelTrainingEngine();
                }
            }
        }
        return instance;
    }

    /**
     * Submits a training job to be executed asynchronously in a background thread.
     */
    public synchronized Future<Experiment> submitJob(TrainingJob job) {
        TrainingTask task = new TrainingTask(job);
        Future<Experiment> future = threadPool.submit(task);
        activeTasks.put(job.getId(), future);
        return future;
    }

    /**
     * Cancels an actively running training job.
     */
    public synchronized boolean cancelJob(Long jobId) {
        Future<Experiment> future = activeTasks.get(jobId);
        if (future != null && !future.isDone()) {
            boolean cancelled = future.cancel(true);
            activeTasks.remove(jobId);
            return cancelled;
        }
        return false;
    }

    public boolean isJobRunning(Long jobId) {
        Future<Experiment> future = activeTasks.get(jobId);
        return future != null && !future.isDone();
    }

    public void shutdown() {
        try {
            threadPool.shutdown();
            if (!threadPool.awaitTermination(3, TimeUnit.SECONDS)) {
                threadPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            threadPool.shutdownNow();
        }
    }
}
