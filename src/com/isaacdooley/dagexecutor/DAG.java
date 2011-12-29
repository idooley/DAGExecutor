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

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ArrayListMultimap;

/**
 * A class that can represent a directed-acyclic-graph (DAG) consisting of tasks
 * (Runnable objects) and their dependencies (on other Runnable objects in the
 * graph).
 */
public class DAG {

	private final HashSet<Runnable> _tasks = new HashSet<Runnable>();
	private final ArrayListMultimap<Runnable, Runnable> _dependencies = ArrayListMultimap
			.create();
	private Map<Runnable, Throwable> _errors = null;

	public enum Status {
		/** All tasks were successfully scheduled. */
		COMPLETED_ALL_TASKS,
		/** Some tasks resulted in errors. Check getErrors() for the errors. */
		ERRORS,
		/**
		 * The dependencies formed a cycle resulting in some tasks not being
		 * able to be scheduled.
		 */
		INVALID_DEPENDENCIES
	}

	/**
	 * Determines the status of this graph. Call this only after the DAG has
	 * been been executed by a DAGExecutor.
	 */
	public synchronized Status status() {
		if (_tasks.size() == 0)
			return Status.COMPLETED_ALL_TASKS;
		if (_errors != null)
			return Status.ERRORS;
		if (_tasks.size() > 0)
			return Status.INVALID_DEPENDENCIES;
		throw new RuntimeException("entered unknown state");
	}

	/** Returns a mapping from failed tasks to the exceptions each threw. */
	public synchronized Map<Runnable, Throwable> getErrors() {
		return _errors;
	}

	/** Find the next runnable task, without removing it from _tasks */
	private synchronized Runnable peekNextRunnableTask() {
		for (Runnable t : _tasks) {
			if (_dependencies.containsKey(t)) {
				List<Runnable> v = _dependencies.get(t);
				if (v.isEmpty())
					return t;
			} else {
				return t;
			}
		}
		return null;
	}

	/**
	 * Determine if there is a task that can now be run, because it has no
	 * outstanding unfinished dependencies
	 */
	public synchronized boolean hasNextRunnableTask() {
		return peekNextRunnableTask() != null;
	}

	/**
	 * Determine if there are any remaining tasks in this executor. If
	 * hasNextRunnableTask() has returned true, then these remaining tasks
	 * cannot be scheduled due to failed dependencies or cycles in the graph.
	 */
	public synchronized boolean hasTasks() {
		return _tasks.size() > 0;
	}

	/** Add an in-degree-zero task to this graph. */
	public synchronized void insert(Runnable task) {
		_tasks.add(task);
	}

	/** Add a task that depends upon another specified task to this DAG. **/
	public synchronized void insert(Runnable task, Runnable dependency) {

		_tasks.add(task);
		_dependencies.put(task, dependency);
	}

	/** Add a task that depends upon a set of tasks to this DAG. **/
	public synchronized void insert(Runnable task, Set<Runnable> dependencies) {
		_tasks.add(task);
		_dependencies.putAll(task, dependencies);
	}

	public synchronized Runnable nextRunnableTask() {
		Runnable r = peekNextRunnableTask();
		_tasks.remove(r);
		return r;
	}

	public synchronized void notifyDone(Runnable task) {
		// Remove t from the list of remaining dependencies for any other tasks.
		_dependencies.values().remove(task);
	}

	public synchronized void notifyError(Runnable r, Throwable error) {
		_errors.put(r, error);
	}

	public int numTasks() {
		return _tasks.size();
	}

	/**
	 * Verify the validity of the DAG, throwing exceptions if invalid
	 * dependencies are found.
	 */
	public void verifyValidGraph() throws DependencyDoesNotExistException {
		for (Runnable d : _dependencies.values()) {
			if (!_tasks.contains(d)) {
				throw new DependencyDoesNotExistException(d);
			}
		}
	}

}
