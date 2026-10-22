package org.javamaster.mybatis.tools.rename

import com.baomidou.plugin.idea.mybatisx.util.DomUtils
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.lang.ognl.psi.OgnlExpression
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.tree.injected.InjectedCaret
import com.intellij.psi.util.PsiUtil
import com.intellij.refactoring.rename.PsiElementRenameHandler
import com.intellij.refactoring.rename.RenameHandler

/**
 * @author yudong
 */
class OgnlRenameHandler : RenameHandler {

    override fun invoke(project: Project, editor: Editor?, file: PsiFile?, dataContext: DataContext) {
        editor ?: return

        val ognlExpression = getOgnlExpression(editor, project) ?: return

        val elementToRename = PsiElementRenameHandler.getElement(dataContext) ?: return

        OgnlRenameDialog(project, elementToRename, ognlExpression, editor).show()
    }

    override fun invoke(project: Project, elements: Array<out PsiElement>, dataContext: DataContext?) {

    }

    override fun isAvailableOnDataContext(dataContext: DataContext): Boolean {
        val editor = CommonDataKeys.EDITOR.getData(dataContext) ?: return false

        val project = CommonDataKeys.PROJECT.getData(dataContext) ?: return false

        return getOgnlExpression(editor, project) != null
    }

    private fun getOgnlExpression(editor: Editor, project: Project): OgnlExpression? {
        val currentCaret = editor.caretModel.currentCaret
        if (currentCaret !is InjectedCaret) {
            return null
        }

        val offset = currentCaret.delegate.offset

        val virtualFile = editor.virtualFile ?: return null

        val psiFile = PsiUtil.getPsiFile(project, virtualFile)

        if (!DomUtils.isMybatisFile(psiFile)) {
            return null
        }

        val manager = InjectedLanguageManager.getInstance(project)

        val injectedElement = manager.findInjectedElementAt(psiFile, offset) ?: return null

        return injectedElement.parent as? OgnlExpression
    }
}
