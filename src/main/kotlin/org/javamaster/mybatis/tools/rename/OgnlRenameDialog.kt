package org.javamaster.mybatis.tools.rename

import com.intellij.lang.ognl.psi.OgnlExpression
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.refactoring.rename.RenameDialog

/**
 * @author yudong
 */
class OgnlRenameDialog(
    project: Project,
    psiElement: PsiElement,
    val nameSuggestionContext: OgnlExpression,
    val editor: Editor,
) : RenameDialog(project, psiElement, nameSuggestionContext, editor) {

    override fun doAction() {
        val newName = getNewName()

        close(0)

        val module = ModuleUtilCore.findModuleForPsiElement(psiElement) ?: return

        val searchScope = GlobalSearchScope.moduleWithDependenciesScope(module)

        val searchParameters = ReferencesSearch.SearchParameters(psiElement, searchScope, true)

        val query = ReferencesSearch.search(searchParameters)

        WriteCommandAction.runWriteCommandAction(
            project,
            "Rename OGNL Expression",
            null,
            {
                query.forEach { it.handleElementRename(newName) }
            },
            psiElement.containingFile
        )

        val renameDialog = RenameDialog(project, psiElement, nameSuggestionContext, editor)
        renameDialog.performRename(newName)
    }


    override fun hasPreviewButton(): Boolean {
        return false
    }

}
