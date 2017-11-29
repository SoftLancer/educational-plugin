package com.jetbrains.edu.learning.stepic

import com.intellij.openapi.project.Project
import com.jetbrains.edu.learning.EduSettings
import com.jetbrains.edu.learning.StudyTaskManager
import com.jetbrains.edu.learning.checker.CheckListener
import com.jetbrains.edu.learning.checker.CheckResult
import com.jetbrains.edu.learning.courseFormat.CheckStatus
import com.jetbrains.edu.learning.courseFormat.tasks.EduTask
import com.jetbrains.edu.learning.courseFormat.tasks.Task

class PostSolutionCheckListener : CheckListener {
    override fun afterCheck(project: Project, task: Task, result: CheckResult) {
        val course = StudyTaskManager.getInstance(project).course
        val status = task.status
        if (EduSettings.getInstance().user != null && course != null && course.isStudy && status != CheckStatus.Unchecked && task is EduTask) {
            StepicConnector.postSolution(task, status == CheckStatus.Solved, project)
        }
    }
}