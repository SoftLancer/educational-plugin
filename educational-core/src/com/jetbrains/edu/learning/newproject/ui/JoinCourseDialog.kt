package com.jetbrains.edu.learning.newproject.ui

import com.intellij.openapi.ui.DialogWrapper
import com.jetbrains.edu.learning.configuration.EduConfiguratorManager
import com.jetbrains.edu.learning.courseFormat.Course
import javax.swing.JComponent

class JoinCourseDialog(private val course: Course) : DialogWrapper(true) {

  private val panel: JoinCoursePanel = JoinCoursePanel()

  init {
    title = course.name
    setOKButtonText("Join")
    panel.bindCourse(course)
    panel.setValidationListener(object : JoinCoursePanel.ValidationListener {
      override fun onInputDataValidated(isInputDataComplete: Boolean) {
        isOKActionEnabled = isInputDataComplete
      }

    })
    init()
  }

  override fun createCenterPanel(): JComponent = panel

  override fun doOKAction() {
    val location = panel.locationString
    val projectSettings = panel.projectSettings
    val language = course.languageById
    if (language != null) {
      EduConfiguratorManager.forLanguageAndCourseType(language, course.courseType)
              ?.courseBuilder
              ?.getCourseProjectGenerator(course)
              ?.doCreateCourseProject(location, projectSettings)
    }
    close(OK_EXIT_CODE)
  }
}