package edu.lafayette.vcache.core;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import edu.lafayette.vcache.core.view.CacheView;
import edu.lafayette.vcache.core.view.ViewUpdater;
import edu.lafayette.vcache.util.Debug;

public class Updater implements ViewUpdater {
	public static final int ACCESS = 0, STEP = ACCESS + 1, ANIMATION = STEP + 1, LEVELS_SUPPORTED = ANIMATION + 1, ANIMATION_CORRECTION = 4000;
	public static final int INITIAL = CacheAccess.INITIAL, LOOKUP_HIT = CacheAccess.LOOKUP_HIT, LOOKUP_MISS = CacheAccess.LOOKUP_MISS,
					COMPLETE_HIT = CacheAccess.COMPLETE_HIT, COMPLETE_MISS = CacheAccess.COMPLETE_MISS, NONE = CacheAccess.NONE,
					HIGHER_LEVEL_WRITE = NONE + 1, HIGHER_LEVEL_READ = HIGHER_LEVEL_WRITE + 1;
	private ArrayList<LinkedList<CacheView>> viewList;
	public static int PLAY = 0, PAUSE = 1, NEXT_STEP = 2;
	private int unitAnimationDelay; //in ms
	private Map<Integer, AnimationInterval> animationIntervalMap;
	private int updateStage, correctedNumOfUpdates, controlStatus;
	private float status;
	private Timer timer;
	private boolean timerEnabled;
	private boolean write, read;
	private Cache cache;
	private CacheTrace<?> trace;
	private Thread readWriteHaltThread;
	private Thread readWriteTimerThread;
	
	public Updater(Cache cache, int unitAnimationDelay, Map<Integer, AnimationInterval> animationIntervalMap) {
		this.cache = cache;
		cache.setUpdater(this);
		viewList = new ArrayList<LinkedList<CacheView>>(3);
		for (int i = 0; i < LEVELS_SUPPORTED; i++)
			viewList.add(new LinkedList<CacheView>());
		this.unitAnimationDelay = unitAnimationDelay;
		this.animationIntervalMap = animationIntervalMap;
		updateStage = 0;
		controlStatus = PAUSE;
		timerEnabled = true;
		write = false;
		read = false;
		readWriteHaltThread = Thread.currentThread();
	}
	
	public void setTrace(CacheTrace<?> trace) {
		this.trace = trace;
	}
	
	public void addView(CacheView view, int level) {
		removeView(view);
		viewList.get(level).add(view);
		
	}

	public void removeView(CacheView view) {
		for (int i = 0; i < LEVELS_SUPPORTED; i++)
			viewList.get(i).remove(view);
		
	}
	
	public void completeAccess() {
		if (timerEnabled)
			startTimer();
		else
			loop();
	}
	
	//precondition: !timerEnabled
	private void loop() {
		status = 0;
		do {
			updateViews(ACCESS);
			while (cache.getCurrentAccess().getStatus() != CacheAccess.COMPLETE_HIT && cache.getCurrentAccess().getStatus() != CacheAccess.COMPLETE_MISS) {
				cache.getCurrentAccess().nextStep();
				updateViews(STEP);
			}
		} while (controlStatus == PLAY && nextAccess());
	}
	
	//precondition: timerEnabled
	private void startTimer() {
//		System.out.println("startTimer entry. Thread = " + Thread.currentThread().getName());
		CacheAccess currentAccess = cache.getCurrentAccess();
		if(currentAccess == null || currentAccess.getStatus() == CacheAccess.NONE)
			return;
		AnimationInterval interval = animationIntervalMap.get(getAccessStatus());
		
		correctedNumOfUpdates = (int)(interval.getNumOfUpdates() * (unitAnimationDelay * interval.intervalDelayUnits) / ANIMATION_CORRECTION);
		if (interval.getNumOfUpdates() == 1 || correctedNumOfUpdates < 1)
			correctedNumOfUpdates = 1;
		int delay = (int) (unitAnimationDelay * interval.intervalDelayUnits / correctedNumOfUpdates);
		updateStage = 0;
		status = 0;
//		System.out.println("startTimer updateViews(ACCESS) calling. Thread = " + Thread.currentThread().getName());
		updateViews(ACCESS);
//		System.out.println("startTimer updateViews(ACCESS) called. Thread = " + Thread.currentThread().getName());
		timer = new Timer();
//		System.out.println("Timer Starting. Delay = " + delay);
		if (correctedNumOfUpdates == 1)
			timer.schedule(new UpdaterTimerTask(), delay);
		else
			timer.schedule(new UpdaterTimerTask(), delay, delay);
//		System.out.println("startTimer exit. Thread = " + Thread.currentThread().getName());
	}
	
	private boolean nextAccess() {
		if (trace != null && trace.hasMoreAccess()) {
			trace.nextAccess();
			return true;
		}
		return false;
	}
	
	synchronized void write(CacheLine line) {
		write = true;
		completeMemoryOperation();
	}
	
	synchronized void read(CacheLine line) {
		read = true;
		completeMemoryOperation();
	}
	
	//precondition: set read or write accordingly
	private void completeMemoryOperation() {
		updateViews(STEP);
		if (timerEnabled) {
			readWriteHaltThread = Thread.currentThread();
			startTimer();
			while (read || write) {
				try {
					if (Thread.interrupted())
						throw new InterruptedException();
					Thread.sleep(1000);
				} catch (InterruptedException ie) {}
			}
			Thread.interrupted();
			try {
				readWriteTimerThread.join();
			} catch (InterruptedException e) {}
		}
	}

	public synchronized int getUnitAnimationDelay() {
		return unitAnimationDelay;
	}
	
	public synchronized void setUnitAnimationDelay(int delay) {
		unitAnimationDelay = delay;
	}
	
	public float getStatus() {
		return status;
	}
	
	public synchronized int getControlStatus() {
		return controlStatus;
	}

	public int getAccessStatus() {
		if (read)
			return HIGHER_LEVEL_READ;
		if (write)
			return HIGHER_LEVEL_WRITE;
		if (cache.getCurrentAccess() == null)
			return INITIAL;
		return cache.getCurrentAccess().getStatus();
	}
	
	public void stopTimer() {
		if (timer != null)
			timer.cancel();
	}
	
	public void setControlStatus(int status) {
		if (controlStatus == status)
			return;
		controlStatus = status;
		if (controlStatus == PLAY || controlStatus == NEXT_STEP) {
			if (nextAccess())
				completeAccess();
		}
	}
	
	public static class AnimationInterval {
		//the delay from this interval to start to end. The actual delay is the product of intervalDelayUnits and unitAnimationDelay
		private float 	intervalDelayUnits;
		private int		numOfUpdates; 	//how many times the connected VCacheAnimationView's update method will be called during an 
										//interval. 0 indicates only at the start and at the of the interval update will be called.
//						increment; 		//specifies how much the status code will increment at the end of the interval, default is 1
		
		public AnimationInterval(float intervalDelayUnits, int numOfUpdates) {
			this.intervalDelayUnits = intervalDelayUnits;
			this.numOfUpdates = numOfUpdates;
		}

		public float getIntervalDelayUnits() {
			return intervalDelayUnits;
		}

		public int getNumOfUpdates() {
			return numOfUpdates;
		}
	}
	
	private void updateViews(int level) {
//		System.out.println("updateViews entry. Thread = " + Thread.currentThread().getName());
		for (int l = level; l < LEVELS_SUPPORTED; l++)
			for (CacheView view : viewList.get(l)) {
				if (Debug.debug) {
					long t = System.nanoTime();
					System.out.println("VCacheAnimation updating (time between = " + (t - edu.lafayette.vcache.util.Timer.time) / 1000000 + "ms)");
					view.update();
					System.out.println("VCacheAnimation updated (" + ((edu.lafayette.vcache.util.Timer.time = System.nanoTime()) - t) / 1000000 + "ms) " + view);
				} else
					view.update();
			}
//		System.out.println("updateViews exit. Thread = " + Thread.currentThread().getName());
	}
	
	private class UpdaterTimerTask extends TimerTask {
		@Override
		public void run() {		
//			System.out.println("TimerTask entry. Thread = " + Thread.currentThread().getName());
			
			if (updateStage + 1 == correctedNumOfUpdates) {
				timer.cancel();
				if (Debug.debug)
					System.out.println("VCacheAnimation action timer stop (" + (System.nanoTime() - edu.lafayette.vcache.util.Timer.time) / 1000000 + "ms)");	
			}
			status = (float) ++updateStage / correctedNumOfUpdates;

			if (status >= 1) {

				if (read || write) {
					read = false;
					write = false;
					readWriteTimerThread = Thread.currentThread();
					readWriteHaltThread.interrupt();
				} else if (cache.getCurrentAccess().getStatus() == CacheAccess.COMPLETE_HIT || cache.getCurrentAccess().getStatus() == CacheAccess.COMPLETE_MISS) {
					if (controlStatus == PLAY && nextAccess()) {
						updateViews(STEP);

						startTimer();
					}
					else
						return;
				} else {

					cache.getCurrentAccess().nextStep();
					updateViews(STEP);
					startTimer();
				}
			} else
				updateViews(ANIMATION);	
//			System.out.println("TimerTask exit. Thread = " + Thread.currentThread().getName());
			
		}	
	}
}