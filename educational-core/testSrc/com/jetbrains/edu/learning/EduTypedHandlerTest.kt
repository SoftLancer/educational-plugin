package com.jetbrains.edu.learning

import com.intellij.openapi.editor.CaretState
import com.intellij.openapi.editor.LogicalPosition
import junit.framework.TestCase


class EduTypedHandlerTest : EduTestCase() {
  fun `test delete selection`() {
    configureByTaskFile(1, 1, "Task.kt")
    myFixture.editor.caretModel.caretsAndSelections = listOf(
      CaretState(
      LogicalPosition(1, 1),
      LogicalPosition(1, 1),
      LogicalPosition(1, 10)
    ))
    myFixture.performEditorAction("EditorDelete")
    myFixture.editor.selectionModel.removeSelection()
    TestCase.assertEquals("def f():\n" +
                                   "  print(1)", myFixture.editor.document.text)
  }

  fun `test backspace selection`() {
    configureByTaskFile(1, 1, "Task.kt")
    myFixture.editor.caretModel.caretsAndSelections = listOf(
      CaretState(
      LogicalPosition(1, 1),
      LogicalPosition(1, 1),
      LogicalPosition(1, 10)
    ))
    myFixture.performEditorAction("EditorBackSpace")
    myFixture.editor.selectionModel.removeSelection()
    TestCase.assertEquals("def f():\n" +
                                   "  print(1)", myFixture.editor.document.text)
  }

  fun `test cut selection`() {
    configureByTaskFile(1, 1, "Task.kt")
    myFixture.editor.caretModel.caretsAndSelections = listOf(
      CaretState(
      LogicalPosition(1, 1),
      LogicalPosition(1, 1),
      LogicalPosition(1, 10)
    ))
    myFixture.performEditorAction("EditorCut")
    myFixture.editor.selectionModel.removeSelection()
    TestCase.assertEquals("def f():\n" +
                                   "  print(1)", myFixture.editor.document.text)
  }

  fun `test cut selection in placeholder`() {
    configureByTaskFile(1, 1, "Task.kt")
    myFixture.editor.caretModel.caretsAndSelections = listOf(
      CaretState(
      LogicalPosition(1, 4),
      LogicalPosition(1, 4),
      LogicalPosition(1, 6)
    ))
    myFixture.performEditorAction("EditorCut")
    myFixture.editor.selectionModel.removeSelection()
    TestCase.assertEquals("def f():\n" +
                                   "  prt(1)", myFixture.editor.document.text)
  }

  fun `test delete symbol`() {
    configureByTaskFile(1, 2, "Task.kt")
    myFixture.editor.caretModel.moveToOffset(11)
    myFixture.editor.selectionModel.removeSelection()
    myFixture.performEditorAction("EditorDelete")
    TestCase.assertEquals("def f():\n" +
                                   "  rint()", myFixture.editor.document.text)
  }

  fun `test delete symbol in empty placeholder`() {
    configureByTaskFile(1, 3, "Task.kt")
    myFixture.editor.caretModel.moveToOffset(11)
    myFixture.editor.selectionModel.removeSelection()
    myFixture.performEditorAction("EditorDelete")
    TestCase.assertEquals("def f():\n" +
                          "  print()", myFixture.editor.document.text)
  }

  fun `test backspace symbol`() {
    configureByTaskFile(1, 2, "Task.kt")
    myFixture.editor.caretModel.moveToOffset(12)
    myFixture.editor.selectionModel.removeSelection()
    myFixture.performEditorAction("EditorBackSpace")
    TestCase.assertEquals("def f():\n" +
                                   "  rint()", myFixture.editor.document.text)
  }

  fun `test backspace in empty placeholder`() {
    configureByTaskFile(1, 3, "Task.kt")
    myFixture.editor.caretModel.moveToOffset(11)
    myFixture.editor.selectionModel.removeSelection()
    myFixture.performEditorAction("EditorBackSpace")
    TestCase.assertEquals("def f():\n" +
                                   "  print()", myFixture.editor.document.text)
  }

  fun `test cut line`() {
    configureByTaskFile(1, 2, "Task.kt")
    myFixture.editor.caretModel.moveToOffset(12)
    myFixture.editor.selectionModel.removeSelection()
    myFixture.performEditorAction("EditorCut")
    TestCase.assertEquals("def f():\n" +
                                   "  print()", myFixture.editor.document.text)
  }

  fun `test cut line in placeholder`() {
    configureByTaskFile(1, 4, "Task.kt")
    myFixture.editor.caretModel.moveToOffset(12)
    myFixture.editor.selectionModel.removeSelection()
    myFixture.performEditorAction("EditorCut")
    TestCase.assertEquals("def f():\n" +
                                   "  print()", myFixture.editor.document.text)
  }

  fun `test cut line end`() {
    configureByTaskFile(1, 2, "Task.kt")
    myFixture.editor.caretModel.moveToOffset(10)
    myFixture.editor.selectionModel.removeSelection()
    myFixture.performEditorAction("EditorCutLineEnd")
    TestCase.assertEquals("def f():\n" +
                                   "  print()", myFixture.editor.document.text)
  }

  fun `test cut line backward`() {
    configureByTaskFile(1, 2, "Task.kt")
    myFixture.editor.caretModel.moveToOffset(15)
    myFixture.editor.selectionModel.removeSelection()
    myFixture.performEditorAction("EditorCutLineBackward")
    TestCase.assertEquals("def f():\n" +
                          "  print()", myFixture.editor.document.text)
  }


  fun `test delete line end`() {
    configureByTaskFile(1, 2, "Task.kt")
    myFixture.editor.caretModel.moveToOffset(10)
    myFixture.editor.selectionModel.removeSelection()
    myFixture.performEditorAction("EditorDeleteToLineEnd")
    TestCase.assertEquals("def f():\n" +
                          "  print()", myFixture.editor.document.text)
  }

  fun `test delete line start`() {
    configureByTaskFile(1, 2, "Task.kt")
    myFixture.editor.caretModel.moveToOffset(15)
    myFixture.editor.selectionModel.removeSelection()
    myFixture.performEditorAction("EditorDeleteToLineStart")
    TestCase.assertEquals("def f():\n" +
                          "  print()", myFixture.editor.document.text)
  }

  fun `test delete line`() {
    configureByTaskFile(1, 2, "Task.kt")
    myFixture.editor.caretModel.moveToOffset(15)
    myFixture.editor.selectionModel.removeSelection()
    myFixture.performEditorAction("EditorDeleteLine")
    TestCase.assertEquals("def f():\n" +
                          "  print()", myFixture.editor.document.text)
  }

  override fun createCourse() {
    courseWithFiles {
      lesson {
        eduTask {
          taskFile("Task.kt", """
          |def f():
          |  <p>print(1)</p>
        """.trimMargin("|"))
        }
        eduTask {
          taskFile("Task.kt", """
          |def f():
          |  <p>p</p>rint()
        """.trimMargin("|"))
        }
        eduTask {
          taskFile("Task.kt", """
          |def f():
          |  <p></p>print()
        """.trimMargin("|"))
        }
        eduTask {
          taskFile("Task.kt", """
          |def f():
          |  <p>print()
          |  print()</p>
        """.trimMargin("|"))
        }
      }
    }
  }
}