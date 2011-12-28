/** 
 * Copyright 2011 Isaac Dooley
 * @author Isaac Dooley
 */

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.google.common.collect.ArrayListMultimap;

public class DAG{

	HashSet<Runnable> _tasks = new HashSet<Runnable>();
	ArrayListMultimap<Runnable, Runnable> _dependencies = ArrayListMultimap.create();
	Map<Runnable, Throwable> _errors = null;

	public enum Status {
		/** All tasks were successfully scheduled. */
		COMPLETED_ALL_TASKS,
		/** Some tasks resulted in errors. Check getErrors() for the errors. */
		ERRORS,
		/** The dependencies formed a cycle  resulting in some tasks not being able to be scheduled. */
		INVALID_DEPENDENCIES
	}
	
	public synchronized Status status(){
		if(_tasks.size() == 0)
			return Status.COMPLETED_ALL_TASKS;
		if(_errors != null)
			return Status.ERRORS;
		if(_tasks.size() > 0)
			return Status.INVALID_DEPENDENCIES;
		throw new RuntimeException("entered unknown state");
	}
	
	
	public synchronized Map<Runnable, Throwable> getErrors() {
		return _errors;
	}

	public synchronized Runnable peekNextRunnableTask() {
		for(Runnable t : _tasks){
			if(_dependencies.containsKey(t)){
				List<Runnable> v = _dependencies.get(t);
				if(v.isEmpty())
					return t;
			} else {
				return t;
			}
		}
		return null;
	}
	
	public synchronized boolean hasNextRunnableTask() {
		return peekNextRunnableTask() != null;
	}

	/**
	 * Determine if there are any remaining tasks in this executor. If
	 * hasNextRunnableTask() has returned true, then these remaining tasks
	 * cannot be scheduled due to failed dependencies or cycles in the graph.
	 */
	public synchronized boolean hasTasks() {
		return _tasks.size()>0;
	}
	
	public synchronized void insert(Runnable task) {
		_tasks.add(task);
	}

	public synchronized void insert(Runnable task, Runnable dependency) {
		_tasks.add(task);
		_dependencies.put(task, dependency);
	}

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
	
	public synchronized void notifyError(Runnable r, Throwable error){
		_errors.put(r, error);
	}


	public int numTasks() {
		return _tasks.size();
	}

}
