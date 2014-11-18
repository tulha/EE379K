package com.computation.experimental;

import javax.swing.*;
import java.awt.*;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Created by mwkurian on 11/18/2014.
 */
public class OptimalSearch {

    static final int MIN_SIZE = 20000;
    static final int MAX_SIZE = 1500000;
    static final int OFFSET_SIZE = 100000;
    static final int MIN_THREADS = 10;
    static final int MAX_THREADS = 20;
    static final int OFFSET_THREADS = 1;

    public static int DPI_SCALING = 2;

    private JFrame jmf;
    private JLabel label;
    private JProgressBar progressBar;
    private int totalTests = 0;
    private int doneTests = 0;

    public OptimalSearch() {

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {

                JPanel panel = new JPanel(new BorderLayout());
                panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

                jmf = new JFrame("Optimal Search");
                jmf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                jmf.getContentPane().setLayout(new BorderLayout());
                jmf.getContentPane().add(panel);

                label = new JLabel("Warming...");
                label.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
                panel.add(label, BorderLayout.NORTH);

                progressBar = new JProgressBar();
                progressBar.setIndeterminate(true);
                progressBar.setPreferredSize(new Dimension(400 * DPI_SCALING, 45 * DPI_SCALING));
                panel.add(progressBar, BorderLayout.CENTER);


                jmf.pack();
                jmf.setResizable(false);
                jmf.setLocationRelativeTo(null);
                jmf.setVisible(true);
            }
        });
    }

    private static List<Integer> generate(int length) {
        List<Integer> data = new ArrayList<Integer>();
        Random rand = new Random();

        for (int i = 0; i < length; i++) {
            data.add(rand.nextInt());
        }

        return data;
    }

    public void find() {

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                progressBar.setIndeterminate(false);
                progressBar.setValue(0);
            }
        });

        totalTests = ((MAX_THREADS - MIN_THREADS) / OFFSET_THREADS + 1)
                * (MAX_SIZE - MIN_SIZE) / OFFSET_SIZE;
        findHelper(MIN_THREADS, MIN_SIZE, new ArrayList<Result>());
    }

    private void findHelper(final int threads, final int length, final ArrayList<Result> results) {

        if (threads >= MAX_THREADS) {

            // Done
            Collections.sort(results);

            try {
                PrintWriter writer = new PrintWriter("optimal-search.txt", "UTF-8");
                for (Result r : results) {
                    writer.printf("(size: %d)(threads: %d)(time: %fs)\n",
                            r.size, r.threads, r.time);
                }
                writer.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    label.setText("Saved test results to optimal-search.txt");
                }
            });

            return;
        }

        if (length >= MAX_SIZE) {
            findHelper(threads + OFFSET_THREADS, MIN_SIZE, results);
            return;
        }

        final List<Integer> searchData = OptimalSearch.generate(length);

        final ConcurrentSearch cs =
                new ConcurrentSearch<Integer>(searchData, Integer.MIN_VALUE, threads) {
                    @Override
                    public void found(Integer integer) {
                        super.found(integer);
                        doneTests++;
                        final int progress = (int) Math.round(((double) doneTests / (double) totalTests) * 100.0);
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                label.setText("Completed test " + doneTests + " of " + totalTests + "...");
                                progressBar.setIndeterminate(false);
                                progressBar.setValue(progress);
                            }
                        });

                        results.add(new Result(length, threads, elapsed()));
                        findHelper(threads, length + OFFSET_SIZE, results);
                    }
                };

        if (threads == MIN_THREADS) {
            new SimpleSearch<Integer>(searchData, Integer.MIN_VALUE) {
                @Override
                public void found(Integer integer) {
                    super.found(integer);
                    doneTests++;
                    results.add(new Result(length, 1, elapsed()));
                    cs.start();
                }
            }.start();
        } else {
            cs.start();
        }
    }

    private class Result implements Comparable<Result> {
        public final int size;
        public final int threads;
        public final double time;

        public Result(int size, int threads, double time) {
            this.size = size;
            this.threads = threads;
            this.time = time;
        }

        @Override
        public int compareTo(Result o) {
            return Double.compare(time, o.time);
        }
    }

}