package com.isaacdooley.dagexecutor;
/** 
 * Copyright 2011 Isaac Dooley
 * @author Isaac Dooley
 */

import java.util.List;
import java.util.concurrent.TimeUnit;


/** A DAGExecutor that runs tasks one at a time sequentially in the thread that calls submit(). */
public class SingleThreadedDAGExecutor implements DAGExecutor {

	@Override
	public boolean awaitTermination(long timeout, TimeUnit units) {
		return true;
	}

	@Override
	public boolean isShutdown() {
		return true;
	}

	@Override
	public boolean isTerminated() {
		return true;
	}

	@Override
	public void shutdown() {
		// do nothing
	}

	@Override
	public List<Runnable> shutdownNow() {
		return null;
	}

	@Override
	public void submit(DAG taskGraph) {
		while (taskGraph.hasNextRunnableTask()) {
			Runnable t = taskGraph.nextRunnableTask();
			boolean hadError = false;

			try {
				t.run();
			} catch (Throwable err) {
				hadError = true;
				taskGraph.notifyError(t, err);
			}

			if (!hadError) {
				taskGraph.notifyDone(t);
			}
		}
	}

	
	
}
