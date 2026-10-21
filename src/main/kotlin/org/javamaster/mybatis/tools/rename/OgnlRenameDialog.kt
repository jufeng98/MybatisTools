package org.javamaster.mybatis.tools.rename

import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.lang.ognl.OgnlLanguage
import com.intellij.lang.ognl.psi.OgnlExpression
import com.intellij.lang.ognl.psi.OgnlReferenceExpression
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.ElementManipulators
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiLanguageInjectionHost
import com.intellij.psi.PsiReference
import com.intellij.psi.util.PsiTreeUtil
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


        val manager = InjectedLanguageManager.getInstance(project)
        val topLevelFile = manager.getTopLevelFile(nameSuggestionContext)

        val allInjectedOgnlFiles = getAllInjectedOgnlFiles(topLevelFile)

        val references = mutableListOf<PsiReference>()

        for (psiFile in allInjectedOgnlFiles) {
            val expressions = PsiTreeUtil.findChildrenOfType(psiFile, OgnlReferenceExpression::class.java)
            for (expression in expressions) {
                for (reference in expression.references) {
                    val resolve = reference.resolve()
                    if (resolve == psiElement) {
                        references.add(reference)
                    }
                }
            }
        }

        for (reference in references) {
            reference.handleElementRename(newName)
        }

        val manipulator = ElementManipulators.getManipulator(psiElement)
        manipulator?.handleContentChange(psiElement, newName)
    }


    override fun hasPreviewButton(): Boolean {
        return false
    }

    /**
     * 获取 xmlFile 中所有注入的 OGNL 文件（包括多级嵌套注入）。
     */
    fun getAllInjectedOgnlFiles(xmlFile: PsiFile): List<PsiFile> {
        val project: Project = xmlFile.project
        val manager = InjectedLanguageManager.getInstance(project)
        val result = mutableListOf<PsiFile>()
        collectOgnlFilesRecursively(xmlFile, manager, result, visited = mutableSetOf())
        return result
    }

    /**
     * 递归收集 OGNL 注入文件。
     *
     * @param file    当前扫描的文件（宿主 XML / 中间注入的 SQL 文件等）
     * @param manager InjectedLanguageManager
     * @param result  结果收集器
     * @param visited 已访问文件集合，防止递归循环
     */
    private fun collectOgnlFilesRecursively(
        file: PsiFile,
        manager: InjectedLanguageManager,
        result: MutableList<PsiFile>,
        visited: MutableSet<PsiFile>,
    ) {
        // 防止循环注入或重复处理
        if (!visited.add(file)) return

        // 如果当前文件本身就是 OGNL 语言，收集并停止向下（OGNL 内部通常不再嵌套）
        if (isOgnlFile(file)) {
            result.add(file)
            return
        }

        // 找出当前文件中所有可能的注入宿主
        val hosts: Collection<PsiLanguageInjectionHost> =
            PsiTreeUtil.collectElementsOfType(file, PsiLanguageInjectionHost::class.java)

        for (host in hosts) {
            val injectedList = manager.getInjectedPsiFiles(host) ?: continue

            for (pair in injectedList) {
                val injectedElement = pair.first
                val injectedFile: PsiFile = injectedElement.containingFile ?: continue
                collectOgnlFilesRecursively(injectedFile, manager, result, visited)
            }
        }
    }

    /**
     * 判断某个 PsiFile 是否为 OGNL 语言文件。
     */
    private fun isOgnlFile(file: PsiFile): Boolean {
        val language = file.language
        // 优先用 OgnlLanguage.INSTANCE 判断更稳健；若无法引用，则退化为 ID 比较
        return language.isKindOf(OgnlLanguage.INSTANCE) || language.id.equals("OGNL", ignoreCase = true)
    }

}
