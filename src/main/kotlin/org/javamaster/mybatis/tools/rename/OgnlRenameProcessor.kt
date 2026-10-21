package org.javamaster.mybatis.tools.rename

import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.SearchScope
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.refactoring.rename.RenameProcessor
import com.intellij.usageView.UsageInfo

/**
 * @author yudong
 */
class OgnlRenameProcessor(
    project: Project,
    val psiElement: PsiElement,
    val newName: String,
    refactoringScope: SearchScope,
    isSearchInComments: Boolean,
    isSearchTextOccurrences: Boolean,
) : RenameProcessor(project, psiElement, newName, refactoringScope, isSearchInComments, isSearchTextOccurrences) {

    override fun performRefactoring(usages: Array<out UsageInfo?>) {
        val module = ModuleUtilCore.findModuleForPsiElement(psiElement)

        val searchScope = if (module != null) {
            GlobalSearchScope.moduleWithDependenciesScope(module)
        } else {
            GlobalSearchScope.projectScope(psiElement.project)
        }

        val searchParameters = ReferencesSearch.SearchParameters(psiElement, searchScope, true)

        val query = ReferencesSearch.search(searchParameters)

        query.forEach { it.handleElementRename(newName) }

        super.performRefactoring(usages)
    }
}
