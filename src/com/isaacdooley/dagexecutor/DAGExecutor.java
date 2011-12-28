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
