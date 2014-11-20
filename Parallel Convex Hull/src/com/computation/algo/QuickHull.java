package com.computation.algo;

import com.computation.common.ConvexHull;
import com.computation.common.Edge;
import com.computation.common.Point2D;
import com.computation.common.Utils;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class QuickHull extends ConvexHull {

    private ExecutorService executorService;
    private AtomicInteger subsetStartCount;
    private AtomicInteger subsetFinishCount;

    public QuickHull(int points, int width, int height, int threads) {
        super(points, width, height, threads);
    }
    public QuickHull(int points, int width, int height, int threads, boolean debug) {
        super(points, width, height, threads, debug);
    }
    public QuickHull(int points, int width, int height, int threads, boolean debug, int animationDelay) {
        super(points, width, height, threads, debug, animationDelay);
    }

    @Override
    protected void findHull() {

        // Start
        this.executorService = Executors.newFixedThreadPool(threads);
        this.subsetStartCount = new AtomicInteger(0);
        this.subsetFinishCount = new AtomicInteger(0);

        // Set some fields
        pointCloud.setField("ThreadPool", true);

        Point2D p1 = Utils.findMax(points, Utils.Direction.NORTH, 0);
        Point2D p2 = Utils.findMax(points, Utils.Direction.SOUTH, 0);

        p1.setColor(Point2D.VISITED);
        p2.setColor(Point2D.VISITED);

        // Handle left side
        HashSet<Point2D> left = new HashSet<Point2D>();
        HashSet<Point2D> right = new HashSet<Point2D>();

        points.remove(p1);
        points.remove(p2);

        pointCloud.addEdge(new Edge(p1, p2));

        for (Point2D point2D : points) {
            if (Utils.isPointLeftOf(p1, p2, point2D)) {
                left.add(point2D);
            } else {
                right.add(point2D);
            }
        }

        try {
            executorService.execute(new Subset(left, p1, p2));
            executorService.execute(new Subset(right, p2, p1));
        } catch (Exception e) {
            e.printStackTrace();
        }

        synchronized (QuickHull.this){
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        executorService.shutdown();
    }

    private class Subset implements Runnable {

        private final Point2D a;
        private final Point2D b;
        private final Set<Point2D> points;

        public Subset(Set<Point2D> points, Point2D a, Point2D b) {
            this.a = a;
            this.b = b;
            this.points = points;

            int subsets = subsetStartCount.incrementAndGet();
            pointCloud.setField("Subsets", subsets);
        }

        @Override
        public void run() {
            Point2D max = null;
            int dist = Integer.MIN_VALUE;

            // Thread this later
            for (Point2D p : points) {
                int d = Utils.distance(a, b, p);
                if (d > dist) {
                    dist = d;
                    max = p;
                }
            }

            if (max == null) {
                if (subsetStartCount.get() == subsetFinishCount.incrementAndGet()) {
                    synchronized (QuickHull.this){
                        QuickHull.this.notify();
                    }
                }
                return;
            }

            points.remove(max);

            max.setColor(Point2D.VISITED);

            // animation delay
            delay();

            pointCloud.removeEdge(new Edge(a, b));
            pointCloud.addEdge(new Edge(max, a));
            pointCloud.addEdge(new Edge(max, b));

            HashSet<Point2D> left = new HashSet<Point2D>();
            HashSet<Point2D> right = new HashSet<Point2D>();

            for (Point2D point2D : points) {
                if (Utils.isPointLeftOf(a, max, point2D)) {
                    left.add(point2D);
                } else if (!Utils.isPointLeftOf(b, max, point2D)) {
                    right.add(point2D);
                }
            }

            try {
                executorService.execute(new Subset(left, a, max));
                executorService.execute(new Subset(right, max, b));
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                subsetFinishCount.incrementAndGet();
            }
        }
    }
}
