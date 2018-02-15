package com.jetbrains.edu.learning.projectView

import com.intellij.openapi.util.Pair
import com.jetbrains.edu.learning.courseFormat.CheckStatus
import com.jetbrains.edu.learning.courseFormat.Lesson
import com.jetbrains.edu.learning.courseFormat.tasks.TaskWithSubtasks

object ProgressUtil {
  /**
   * Counts current progress for course which consists of only on task with subtasks
   * In this case we count each subtasks as task
   * @return Pair (number of solved tasks, number of tasks) or null if lessons can't be interpreted as one task with subtasks
   */
  fun countProgressAsOneTaskWithSubtasks(lessons: List<Lesson>): Pair<Int, Int>? {
    if (lessons.size == 1 && lessons[0].taskListForProgress.size == 1) {
      val lesson = lessons[0]
      val task = lesson.taskListForProgress[0]
      if (task is TaskWithSubtasks) {
        val lastSubtaskIndex = task.lastSubtaskIndex
        val activeSubtaskIndex = task.activeSubtaskIndex
        val taskNum = lastSubtaskIndex + 1
        val isLastSubtaskSolved = activeSubtaskIndex == lastSubtaskIndex && task.getStatus() == CheckStatus.Solved
        return Pair.create(if (isLastSubtaskSolved) taskNum else activeSubtaskIndex, taskNum)
      }
    }
    return null
  }

  /**
   * @return Pair (number of solved tasks, number of tasks)
   */
  fun countProgressWithoutSubtasks(lessons: List<Lesson>): Pair<Int, Int> {
    var taskNum = 0
    var taskSolved = 0
    for (lesson in lessons) {
      taskNum += lesson.taskListForProgress.size
      taskSolved += getSolvedTasks(lesson)
    }
    return Pair.create(taskSolved, taskNum)
  }

  private fun getSolvedTasks(lesson: Lesson): Int {
    return lesson.taskListForProgress
      .filter { it.status == CheckStatus.Solved }
      .count()
  }
}
