package org.javamaster.mybatis.tools.rename

import com.intellij.lang.ognl.psi.OgnlExpression
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.refactoring.rename.RenameDialog
import com.intellij.refactoring.rename.RenameProcessor

/**
 * @author yudong
 */
class OgnlRenameDialog(
    project: Project,
    psiElement: PsiElement,
    nameSuggestionContext: OgnlExpression,
    val editor: Editor,
) : RenameDialog(project, psiElement, nameSuggestionContext, editor) {

    override fun doAction() {
        val newName = getNewName()

        close(0)

        performRename(newName)
    }

    override fun createRenameProcessor(newName: String): RenameProcessor {
        return OgnlRenameProcessor(
            this.project, this.psiElement, newName,
            this.refactoringScope, this.isSearchInComments, this.isSearchInNonJavaFiles
        )
    }

    override fun hasPreviewButton(): Boolean {
        return false
    }

}
