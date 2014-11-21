package com.computation;

import com.computation.algo.GiftWrapping;
import com.computation.algo.GrahamScan;
import com.computation.algo.GrahamScanParallel;
import com.computation.algo.QuickHull;
import com.computation.common.Point2DCloud;
import com.computation.experimental.OptimalThreadCountFinder;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;

public class Main {

    public static void main(String[] args) throws ClassNotFoundException, UnsupportedLookAndFeelException,
            InstantiationException, IllegalAccessException, InvocationTargetException, InterruptedException {

        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

        OptimalThreadCountFinder.DPI_SCALING =
                Point2DCloud.DPI_SCALING = 2; /* Set display scaling */

//        OptimalThreadCountFinder optimalThreadCountFinder = new OptimalThreadCountFinder();
//        optimalThreadCountFinder.find();

        //Mathew's Concurrent Quick Hull Implementation
//        ConvexHull convexHull = new QuickHull(/* pointCount */ 100, /* width */ 800, /* height */ 600, /* availableThreads */ 10);
//        convexHull.show(\);

        //Kapil's Concurrent Gift Wrapping Implemntation (max at 4 availableThreads)

        final Point2DCloud point2DCloud = new Point2DCloud(1000, 1000, 1000, true);

        point2DCloud.addTopButton("GiftWrapping", new Runnable() {
            @Override
            public void run() {
                new GiftWrapping(point2DCloud, getThreadCount(), true, 100);
            }
        });

        point2DCloud.addTopButton("GrahamScanParallel", new Runnable() {
            @Override
            public void run() {
                new GrahamScanParallel(point2DCloud, getThreadCount(), true, 100);
            }
        });

        point2DCloud.addTopButton("QuickHull", new Runnable() {
            @Override
            public void run() {
                new QuickHull(point2DCloud, getThreadCount(), true, 100);
            }
        });

        point2DCloud.addTopButton("GrahamScan", new Runnable() {
            @Override
            public void run() {
                new GrahamScan(point2DCloud, getThreadCount(), true, 100);
            }
        });

        point2DCloud.show();
    }

    public static int getThreadCount(){
        return Integer.parseInt(JOptionPane.showInputDialog(null, "Number of threads"));
    }
}
