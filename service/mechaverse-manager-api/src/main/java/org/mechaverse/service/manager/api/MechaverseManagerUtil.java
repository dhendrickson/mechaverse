package org.mechaverse.service.manager.api;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.mechaverse.service.manager.api.model.Task;

/**
 * Utility methods for the mechaverse manager.
 */
public final class MechaverseManagerUtil {

  private MechaverseManagerUtil() {}

  /**
   * Returns a list of tasks whose age is less than the given maximum.
   */
  public static List<Task> getActiveTasks(Iterable<Task> tasks, long taskMaxDurationSeconds) {
    List<Task> activeTasks = new ArrayList<>();
    for (Task task : tasks) {
      if (isActive(task, taskMaxDurationSeconds)) {
        activeTasks.add(task);
      }
    }
    return activeTasks;
  }

  /**
   * Returns a list of tasks whose age is greater than the given maximum.
   */
  public static List<Task> getInactiveTasks(Iterable<Task> tasks, long taskMaxDurationSeconds) {
    List<Task> inactiveTasks = new ArrayList<>();
    for (Task task : tasks) {
      if (!isActive(task, taskMaxDurationSeconds)) {
        inactiveTasks.add(task);
      }
    }
    return inactiveTasks;
  }

  /**
   * Returns true if the age of the given task is less than the given maximum, false otherwise.
   */
  public static boolean isActive(Task task, long taskMaxDurationSeconds) {
    long taskMaxDurationMS = taskMaxDurationSeconds * 1000;
    long now = new Date().getTime();
    return (now - task.getStartTime().getTime() < taskMaxDurationMS);
  }

  /**
   * Returns true if at least one task is active.
   */
  public static boolean hasActiveTask(Iterable<Task> tasks, long taskMaxDurationSeconds) {
    for (Task task : tasks) {
      if (isActive(task, taskMaxDurationSeconds)) {
        return true;
      }
    }
    return false;
  }
}
