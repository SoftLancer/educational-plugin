package com.jetbrains.edu.learning.ui.taskDescription.check

import com.intellij.icons.AllIcons
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.JBUI
import com.jetbrains.edu.learning.checker.CheckResult
import com.jetbrains.edu.learning.courseFormat.CheckStatus
import icons.EducationalCoreIcons

class CheckResultLabel(checkResult: CheckResult) : JBLabel() {
  init {
    val status = checkResult.status
    iconTextGap = JBUI.scale(4)
    icon = when (status) {
      CheckStatus.Failed -> AllIcons.General.BalloonError
      CheckStatus.Solved -> EducationalCoreIcons.ResultCorrect
      else -> null
    }
    foreground = when (status) {
      CheckStatus.Failed -> JBColor(0xC7222D, 0xFF5261)
      CheckStatus.Solved -> JBColor(0x368746, 0x499C54)
      else -> foreground
    }

    text = when (status) {
      CheckStatus.Failed -> "Incorrect"
      CheckStatus.Solved -> "Correct"
      else -> ""
    }
    border = JBUI.Borders.empty(8, 16, 0, 0)
  }
}