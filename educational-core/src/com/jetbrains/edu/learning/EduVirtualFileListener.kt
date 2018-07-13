package com.jetbrains.edu.learning

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFileEvent
import com.intellij.openapi.vfs.VirtualFileListener
import com.jetbrains.edu.coursecreator.CCUtils
import com.jetbrains.edu.coursecreator.configuration.YamlFormatSynchronizer
import com.jetbrains.edu.learning.courseFormat.Lesson
import com.jetbrains.edu.learning.courseFormat.Section
import com.jetbrains.edu.learning.courseFormat.ext.configurator
import com.jetbrains.edu.learning.courseFormat.ext.findSourceDir
import com.jetbrains.edu.learning.courseFormat.tasks.Task

abstract class EduVirtualFileListener(protected val project: Project) : VirtualFileListener {

  override fun fileCreated(event: VirtualFileEvent) {
    val fileInfo = event.fileInfo(project) as? FileInfo.FileInTask ?: return
    fileInTaskCreated(event, fileInfo)
  }

  /**
   * Actual text of files is not loaded intentionally
   * because it is required only in some places where it is really needed:
   * course archive creation, loading to Stepik, etc.
   * Such actions load necessary text of files themselves.
   *
   * Also info about new file won't be added if the file is already in the task.
   * Generally, such checks are required because of tests.
   * In real life, project files are created before project opening and virtual file listener initialization,
   * so such situation shouldn't happen.
   * But in tests, course files usually are created by [EduTestCase.courseWithFiles] which triggers virtual file listener because
   * sometimes listener is initialized in `[TestCase.setUp] method and [EduTestCase.courseWithFiles] creates course files after it.
   * In such cases, these checks prevent replacing correct task file
   * with empty (without placeholders, hints, etc.) one.
   */
  protected open fun fileInTaskCreated(event: VirtualFileEvent, fileInfo: FileInfo.FileInTask) {
    val (task, pathInTask, kind) = fileInfo
    when (kind) {
      NewFileKind.TASK_FILE -> {
        if (task.getTaskFile(pathInTask) == null) {
          val taskFile = task.addTaskFile(pathInTask)
          if (EduUtils.isStudentProject(project)) {
            taskFile.isUserCreated = true
          }
        }
      }
      NewFileKind.TEST_FILE -> {
        if (pathInTask !in task.testsText) {
          task.addTestsTexts(pathInTask, "")
        }
      }
      NewFileKind.ADDITIONAL_FILE -> {
        if (pathInTask !in task.additionalFiles) {
          task.addAdditionalFile(pathInTask, "")
        }
      }
    }
  }

  protected fun VirtualFileEvent.fileInfo(project: Project): FileInfo? {
    if (project.isDisposed) return null
    if (file.path.contains(CCUtils.GENERATED_FILES_FOLDER)) return null
    if (YamlFormatSynchronizer.isConfigFile(file)) return null
    val courseDir = EduUtils.getCourseDir(project)
    if (!FileUtil.isAncestor(courseDir.path, file.path, true)) return null
    val course = StudyTaskManager.getInstance(project).course ?: return null
    if (course.configurator?.excludeFromArchive(project, file.path) == true) return null

    if (file.isDirectory) {
      EduUtils.getSection(file, course)?.let { return FileInfo.SectionDirectory(it) }
      EduUtils.getLesson(file, course)?.let { return FileInfo.LessonDirectory(it) }
      EduUtils.getTask(file, course)?.let { return FileInfo.TaskDirectory(it) }
    } else {
      val task = EduUtils.getTaskForFile(project, file) ?: return null
      val taskDir = task.getTaskDir(project) ?: return null

      val taskRelativePath = EduUtils.pathRelativeToTask(project, file)

      if (EduUtils.isTaskDescriptionFile(file.name)
          || taskRelativePath.contains(EduNames.WINDOW_POSTFIX)
          || taskRelativePath.contains(EduNames.WINDOWS_POSTFIX)
          || taskRelativePath.contains(EduNames.ANSWERS_POSTFIX)) {
        return null
      }

      if (EduUtils.isTestsFile(project, file)) return FileInfo.FileInTask(task, taskRelativePath, NewFileKind.TEST_FILE)
      val sourceDir = task.findSourceDir(taskDir)
      if (sourceDir != null) {
        if (VfsUtilCore.isAncestor(sourceDir, file, true)) return FileInfo.FileInTask(task, taskRelativePath, NewFileKind.TASK_FILE)
      }
      return FileInfo.FileInTask(task, taskRelativePath, NewFileKind.ADDITIONAL_FILE)
    }

    return null
  }

  protected sealed class FileInfo {
    data class SectionDirectory(val section: Section) : FileInfo()
    data class LessonDirectory(val lesson: Lesson) : FileInfo()
    data class TaskDirectory(val task: Task) : FileInfo()
    data class FileInTask(val task: Task, val pathInTask: String, val kind: NewFileKind) : FileInfo()
  }

  protected enum class NewFileKind {
    TASK_FILE,
    TEST_FILE,
    ADDITIONAL_FILE
  }
}
