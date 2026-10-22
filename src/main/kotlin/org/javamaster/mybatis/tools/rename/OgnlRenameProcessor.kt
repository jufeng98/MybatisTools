package org.javamaster.mybatis.tools.rename

import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.SearchScope
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.refactoring.rename.RenameProcessor
import com.intellij.refactoring.util.MoveRenameUsageInfo
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

    override fun findUsages(): Array<out UsageInfo?> {
        val usages = super.findUsages().toMutableList()

        val module = ModuleUtilCore.findModuleForPsiElement(psiElement)
        val scope = if (module != null) {
            GlobalSearchScope.moduleWithDependenciesScope(module)
        } else {
            GlobalSearchScope.projectScope(psiElement.project)
        }

        val params = ReferencesSearch.SearchParameters(psiElement, scope, true)

        val references = ReferencesSearch.search(params)

        references.forEach { ref ->
            // 用 element + range 去重，避免和 super.findUsages() 已有结果重复
            val already = usages.any { u ->
                u.element == ref.element && u.rangeInElement == ref.rangeInElement
            }
            if (!already) {
                usages.add(MoveRenameUsageInfo(ref, psiElement))
            }
        }

        return usages.toTypedArray()
    }

}
