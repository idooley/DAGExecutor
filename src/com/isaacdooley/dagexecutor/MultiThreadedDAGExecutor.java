/*
   Copyright 2011 Isaac Dooley

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package com.isaacdooley.dagexecutor;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * A DAGExecutor that runs tasks in separate threads as part of a
 * CachedThreadExecutor.
 */
public class MultiThreadedDAGExecutor implements DAGExecutor {

	/**
	 * A pool of threads for running tasks in the DAG itself.
	 */
	ExecutorService _taskPool = Executors.newCachedThreadPool();
	
	/**
	 * A pool of threads for use in managing the execution of the DAG
	 */
	ExecutorService _managePool = Executors.newCachedThreadPool();

	@Override
	public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
		return	_managePool.awaitTermination(timeout, unit);
	}

	@Override
	public boolean isShutdown() {
		return _managePool.isShutdown();
	}

	@Override
	public boolean isTerminated() {
		return _managePool.isTerminated();
	}

	@Override
	public void shutdown() {
		_managePool.shutdown();
	}

	@Override
	public List<Runnable> shutdownNow() {
		_managePool.shutdownNow();
		_taskPool.shutdownNow();
		// FIXME: return the tasks in the dag that haven't finished
		return null;
	}

	@Override
	public void submit(DAG taskGraph) throws InterruptedException {
		_managePool.execute(new Runner(taskGraph));
	}

	private class Runner implements Runnable {
		final DAG _taskGraph;
		final CountDownLatch _completed = new CountDownLatch(1);

		public Runner(DAG taskGraph) {
			_taskGraph = taskGraph;
		}

		@Override
		public void run() {

			try {
				ArrayBlockingQueue<RunnableWrapper> completionQueue = new ArrayBlockingQueue<RunnableWrapper>(
						_taskGraph.numTasks());
				
				long currentlyExecuting = 0;
				
				while (true) {
					while (_taskGraph.hasNextRunnableTask()) {
						Runnable t = _taskGraph.nextRunnableTask();
						RunnableWrapper wrapper = new RunnableWrapper(t,
								completionQueue);
						currentlyExecuting ++;
						_taskPool.execute(wrapper);
					}

					// Wait for one or more of the tasks to complete
					if(currentlyExecuting>0){
						do {
							RunnableWrapper rw = completionQueue.take();
							currentlyExecuting--;
							if (rw._err == null) {
								_taskGraph.notifyDone(rw._innerTask);
							} else {
								_taskGraph.notifyError(rw._innerTask, rw._err);
							}
						} while (!completionQueue.isEmpty());
					}
					
					// Stop if we encountered any exceptions
					if (_taskGraph.getErrors() != null)
						return;

					// Stop if we have no runnable tasks (perhaps a cycle of
					// non-schedulable tasks remains)
					if (!_taskGraph.hasNextRunnableTask() && currentlyExecuting==0)
						return;

				}

			} catch (InterruptedException e) {
				// do nothing
			} finally {
				_completed.countDown();
			}
		}
	}

	private class RunnableWrapper implements Runnable {
		final Runnable _innerTask;
		Throwable _err = null;
		final ArrayBlockingQueue<RunnableWrapper> _completionQueue;

		RunnableWrapper(Runnable r,
				ArrayBlockingQueue<RunnableWrapper> completionQueue) {
			_innerTask = r;
			_completionQueue = completionQueue;
		}

		@Override
		public void run() {
			try {
				_innerTask.run();
			} catch (Throwable err) {
				_err = err;
			} finally {
				_completionQueue.add(this);
			}
		}

	}

}
