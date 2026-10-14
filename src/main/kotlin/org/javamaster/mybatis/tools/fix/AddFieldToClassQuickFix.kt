package org.javamaster.mybatis.tools.fix

import com.intellij.codeInsight.intention.PriorityAction
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.module.ModuleUtil
import com.intellij.openapi.project.Project
import com.intellij.pom.Navigatable
import com.intellij.psi.PsiElementFactory
import org.javamaster.mybatis.tools.utils.GenerateUtils
import org.javamaster.mybatis.tools.utils.PsiUtils

/**
 * @author yudong
 */
class AddFieldToClassQuickFix(val fieldName: String, val fullClassName: String, val typeName: String) : LocalQuickFix,
    PriorityAction {

    override fun getFamilyName(): String {
        @Suppress("DialogTitleCapitalization")
        return "在 resultType 的类中新增字段"
    }

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val module = ModuleUtil.findModuleForPsiElement(descriptor.psiElement)

        val psiClass = PsiUtils.resolvePsiClass(fullClassName, module, project) ?: return

        FileEditorManager.getInstance(project).openFile(psiClass.containingFile.virtualFile, true)

        val fieldText = GenerateUtils.generateJavaField(fieldName, typeName).trim()

        val elementFactory = PsiElementFactory.getInstance(project)
        val field = elementFactory.createFieldFromText(fieldText, psiClass)

        val element = psiClass.add(field)

        (element as Navigatable).navigate(true)
    }

    override fun getPriority(): PriorityAction.Priority {
        return PriorityAction.Priority.HIGH
    }

}
