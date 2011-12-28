/** 
 * Copyright 2011 Isaac Dooley
 * @author Isaac Dooley
 */

import java.util.List;
import java.util.concurrent.TimeUnit;


public interface DAGExecutor {

	public void submit(DAG taskGraph) throws InterruptedException;
	
	/** @throws InterruptedException 
	 * @retun true if the executor terminated, and false if timed-out before completing all tasks. */
	public boolean awaitTermination(long timeout, TimeUnit units) throws InterruptedException;

	public void shutdown();
	
	public List<Runnable> shutdownNow();
	
	public boolean isShutdown();
	
	public boolean isTerminated();
	
}
