import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import q7.*;

public class Driver {

	static class TimedRunnable implements Runnable {
		
		boolean end = false;
		double time = 0;
		int threadCount = 0;
		String algo = "";
		
		public TimedRunnable(String algo, int threadCount){
			this.algo = algo;
			this.threadCount = threadCount;
		}
		
		
		public void run() {
			if(!end){
				time = System.nanoTime();
				end = true;
				return;
			}
			
			time = (double)(System.nanoTime() - time) / 1000000000.0;
			System.out.printf("%-15s[%d][add]: %fms\n", algo, threadCount, time);
		}
	}
	
	public static void main(String args[]) {
		double executeTimeMS = 0;
		int numThread = 8;

		if (args.length < 2) {
			System.err.println("Provide 3 arguments");
			System.err.println("\t(1) <algorithm>: lock-free/lock-based");
			System.err.println("\t(2) <numThread>: the number of test thread");
			System.exit(-1);
		}
		
		numThread = Integer.parseInt(args[1]);

		for (int threadCount = 1; threadCount <= numThread; threadCount++) {

			LinkedList list = null;
			
			if (args[0].equals("lock-based")) {
				list = new LockBasedList();
			} else if (args[0].equals("lock-free")) {
				list = new LockFreeList();
			} else {
				System.err.println("ERROR: no such algorithm implemented");
				System.exit(-1);
			}
			
			// Create Threads
			Thread[] threads = new Thread[threadCount];
			// Create cyclic barrier for add operation
			CyclicBarrier addCb = new CyclicBarrier(threadCount, new TimedRunnable(args[0], threadCount));
			// Create cyclic barrier for combo operation
			CyclicBarrier comboCb = new CyclicBarrier(threadCount, new TimedRunnable(args[0], threadCount));
			
			for (int i = 0; i < threads.length; i++) {
				threads[i] = new Thread(createRunnable((5000 + threadCount) / threadCount, (25000 + threadCount) / threadCount, addCb, comboCb, list));
			}

			// Start time
			executeTimeMS = System.nanoTime();

			// Start Threads
			for (Thread thread : threads) {
				thread.start();
			}

			// Join threads
			for (Thread thread : threads) {
				try {
					thread.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			// End time
			executeTimeMS = (double)(System.nanoTime() - executeTimeMS) / 1000000000.0;

			System.out.printf("%-15s[%d][total]: %fms\n", args[0], threadCount, executeTimeMS);
		}
	}

	public static Runnable createRunnable(final int toAdd, final int toContainsRemoveAdd, final CyclicBarrier addCb, final CyclicBarrier comboCb,
			final LinkedList list) {
		return new Runnable() {

			Random rand = new Random();

			@Override
			public void run() {

				int threadId = (int) Thread.currentThread().getId();

				// Wait for threads to finish
				try {
					addCb.await();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (BrokenBarrierException e) {
					e.printStackTrace();
				}
				
				// Add random number
				for (int i = 0; i < toAdd; i++) {
					list.add((rand.nextInt() * threadId) % 100001);
				}

				// Wait for threads to finish
				try {
					addCb.await();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (BrokenBarrierException e) {
					e.printStackTrace();
				}
				

				// Wait for threads to finish
				try {
					comboCb.await();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (BrokenBarrierException e) {
					e.printStackTrace();
				}
				
				// Combination of methods
				for (int i = 0; i < toContainsRemoveAdd; i++) {
					double prob = rand.nextDouble();
					int value = (rand.nextInt() * threadId) % 100001;
					if (prob <= 0.4) {
						list.add(value);
					} else if (prob > 0.5) {
						list.contains(value);
					} else {
						list.remove(value);
					}
				}

				// Wait for threads to finish
				try {
					comboCb.await();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (BrokenBarrierException e) {
					e.printStackTrace();
				}
			}
		};
	}
}