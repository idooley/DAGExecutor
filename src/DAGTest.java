/** 
 * Copyright 2011 Isaac Dooley
 * @author Isaac Dooley
 */

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class DAGTest {

	List<String> _result = Collections.synchronizedList(new ArrayList<String>());
	
	/** Create a DAG and pretend to schedule some tests */
	@Test (timeout = 2000)
	public void test() {

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
	public void testSinglethreaded() throws InterruptedException {
		SingleThreadedDAGExecutor executor = new SingleThreadedDAGExecutor();
		testExecutor(executor);
	}

	@Test(timeout = 2000)
	public void testMultithreaded() throws InterruptedException {
		MultiThreadedDAGExecutor executor = new MultiThreadedDAGExecutor();
		testExecutor(executor);
	}
	

	@Test(timeout = 2000)
	public void testSinglethreadedCycle() throws InterruptedException {
		SingleThreadedDAGExecutor executor = new SingleThreadedDAGExecutor();
		testCycleExecutor(executor);
	}

	@Test(timeout = 2000)
	public void testMultithreadedCycle() throws InterruptedException {
		MultiThreadedDAGExecutor executor = new MultiThreadedDAGExecutor();
		testCycleExecutor(executor);
	}

	/** Create a DAG and pretend to schedule some tests  */
	public void testExecutor(DAGExecutor executor) throws InterruptedException {

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
	public void testCycleExecutor(DAGExecutor executor) throws InterruptedException {

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
	
	
	public class Task implements Runnable {

		private final String _name;
		
		public Task(String name){
			_name = name;
		}
		
		@Override
		public void run() {
			_result.add(_name);
		}
		
	}

}
