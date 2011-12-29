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

package com.isaacdooley.dagexecutortests;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;

import com.isaacdooley.dagexecutor.DAG;
import com.isaacdooley.dagexecutor.DAGExecutor;
import com.isaacdooley.dagexecutor.DependencyDoesNotExistException;
import com.isaacdooley.dagexecutor.MultiThreadedDAGExecutor;
import com.isaacdooley.dagexecutor.SingleThreadedDAGExecutor;


public class DAGTest {

	List<String> _result = Collections.synchronizedList(new ArrayList<String>());
	
	/** Create a DAG and pretend to schedule some tests  */
	@Test (timeout = 2000)
	public void dagPretendSchedule() throws DependencyDoesNotExistException {

		DAG dag = new DAG();
		Assert.assertFalse(dag.hasTasks());

		Task t0 = new Task("t0");
		Task t1 = new Task("t1");
		Task t2 = new Task("t2");
		Task t3 = new Task("t3");

		dag.insert(t3);
		Assert.assertTrue(dag.hasTasks());

		dag.insert(t2, t3);
		dag.insert(t1, t2);
		dag.insert(t0, t1);
	
		Assert.assertNull(dag.getErrors());
		Assert.assertTrue(dag.hasNextRunnableTask());
		Assert.assertEquals(t3,dag.nextRunnableTask());
		dag.notifyDone(t3);

		Assert.assertTrue(dag.hasNextRunnableTask());
		Assert.assertEquals(t2,dag.nextRunnableTask());
		dag.notifyDone(t2);

		Assert.assertTrue(dag.hasNextRunnableTask());
		Assert.assertEquals(t1,dag.nextRunnableTask());
		dag.notifyDone(t1);
		
		Assert.assertTrue(dag.hasNextRunnableTask());
		Assert.assertEquals(t0,dag.nextRunnableTask());
		dag.notifyDone(t0);
		
		Assert.assertFalse(dag.hasNextRunnableTask());
		Assert.assertNull(dag.getErrors());
		Assert.assertEquals(DAG.Status.COMPLETED_ALL_TASKS,dag.status());

	}

	@Test(timeout = 2000)
	public void singlethreaded() 
	throws InterruptedException, DependencyDoesNotExistException {
		SingleThreadedDAGExecutor executor = new SingleThreadedDAGExecutor();
		testExecutor(executor);
	}

	@Test(timeout = 2000)
	public void multithreaded() 
	throws InterruptedException, DependencyDoesNotExistException {
		MultiThreadedDAGExecutor executor = new MultiThreadedDAGExecutor();
		testExecutor(executor);
	}
	

	@Test(timeout = 2000)
	public void singlethreadedCycle() 
	throws InterruptedException, DependencyDoesNotExistException {
		SingleThreadedDAGExecutor executor = new SingleThreadedDAGExecutor();
		testCycleExecutor(executor);
	}

	@Test(timeout = 2000)
	public void multithreadedCycle() 
	throws InterruptedException, DependencyDoesNotExistException {
		MultiThreadedDAGExecutor executor = new MultiThreadedDAGExecutor();
		testCycleExecutor(executor);
	}

	/** Create a DAG and pretend to schedule some tests  */
	@Test(timeout = 2000)
	public void variableLengthTasks() throws InterruptedException, DependencyDoesNotExistException {
		MultiThreadedDAGExecutor executor = new MultiThreadedDAGExecutor();

		_result = new ArrayList<String>();
		DAG dag = new DAG();
		Assert.assertFalse(dag.hasTasks());

		Task t0 = new Task("t0", 000);
		Task t1 = new Task("t1", 100);
		Task t2 = new Task("t2", 100);
		Task t3 = new Task("t3", 150);

		dag.insert(t0);
		dag.insert(t1, t0);
		dag.insert(t2, t1);
		dag.insert(t3);
		
		executor.submit(dag);
		executor.shutdown();
		executor.awaitTermination(1, TimeUnit.SECONDS);
		
		Assert.assertFalse(dag.hasNextRunnableTask());
		Assert.assertNull(dag.getErrors());
		Assert.assertEquals(DAG.Status.COMPLETED_ALL_TASKS,dag.status());
		
		String[] expecteds = {"t0", "t1", "t3", "t2"};
		Assert.assertArrayEquals(expecteds, _result.toArray());
		
	}
	
	/** Create a DAG and pretend to schedule some tests  */
	public void testExecutor(DAGExecutor executor) throws InterruptedException, DependencyDoesNotExistException {

		_result = new ArrayList<String>();
		DAG dag = new DAG();
		Assert.assertFalse(dag.hasTasks());

		Task t0 = new Task("t0");
		Task t1 = new Task("t1");
		Task t2 = new Task("t2");
		Task t3 = new Task("t3");

		dag.insert(t3);
		dag.insert(t2, t3);
		dag.insert(t1, t2);
		dag.insert(t0, t1);
		
		executor.submit(dag);
		executor.shutdown();
		executor.awaitTermination(1, TimeUnit.SECONDS);
		
		Assert.assertFalse(dag.hasNextRunnableTask());
		Assert.assertNull(dag.getErrors());
		Assert.assertEquals(DAG.Status.COMPLETED_ALL_TASKS,dag.status());
		
		String[] expecteds = {"t3", "t2", "t1", "t0"};
		Assert.assertArrayEquals(expecteds, _result.toArray());
		
	}
	

	/** Create a DAG and pretend to schedule some tests  */
	public void testCycleExecutor(DAGExecutor executor) 
	throws InterruptedException, DependencyDoesNotExistException {

		_result = new ArrayList<String>();
		DAG dag = new DAG();
		Assert.assertFalse(dag.hasTasks());

		int numTasks = 10;
		Task[] tasks = new Task[numTasks];
		for(int i=0; i< numTasks; i++){
			tasks[i] = new Task("t" + i);
		}

		for(int i=0; i< numTasks; i++){
			dag.insert(tasks[i], tasks[(i+1)%numTasks]);
		}
		
		executor.submit(dag);
	
		Assert.assertFalse(dag.hasNextRunnableTask());
		Assert.assertNull(dag.getErrors());
		Assert.assertEquals(DAG.Status.INVALID_DEPENDENCIES,dag.status());
		
		String[] expecteds = {};
		Assert.assertArrayEquals(expecteds, _result.toArray());
		
	}
	
	

	@Test(timeout = 2000)
	public void testDeadlock() 
	throws InterruptedException, DependencyDoesNotExistException {

		_result = new ArrayList<String>();
		
		// Build DAG that is a cycle
		DAG dag = new DAG();
		int numTasks = 10;
		Task[] tasks = new Task[numTasks];
		for(int i=0; i< numTasks; i++){
			tasks[i] = new Task("t" + i);
		}
		for(int i=0; i< numTasks; i++){
			dag.insert(tasks[i], tasks[(i+1)%numTasks]);
		}
		
		MultiThreadedDAGExecutor executor = new MultiThreadedDAGExecutor();
		executor.submit(dag);
		executor.shutdown();
		Assert.assertTrue(executor.awaitTermination(500, TimeUnit.MILLISECONDS));
		
		Assert.assertEquals(DAG.Status.INVALID_DEPENDENCIES,dag.status());
		
		String[] expecteds = {};
		Assert.assertArrayEquals(expecteds, _result.toArray());
		
	}
	
	public class Task implements Runnable {

		private final String _name;
		private final long _sleepMillis;
		
		public Task(String name){
			_name = name;
			_sleepMillis = 0;
		}
		
		public Task(String name, long sleepMillis) { 
			_name = name;
			_sleepMillis = sleepMillis;
		}
		
		@Override
		public void run() {
			if(_sleepMillis>0){
				try {
					Thread.sleep(_sleepMillis);
				} catch (InterruptedException e) {
					// do nothing
				}
			}
			_result.add(_name);
		}
		
	}

}
