package org.javamaster.mybatis.tools.fix

import com.intellij.codeInsight.intention.PriorityAction
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.module.ModuleUtil
import com.intellij.openapi.project.Project
import com.intellij.pom.Navigatable
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiElementFactory
import com.intellij.psi.search.GlobalSearchScope
import org.javamaster.mybatis.tools.utils.GenerateUtils

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
        val module = ModuleUtil.findModuleForFile(descriptor.psiElement.containingFile) ?: return

        val scope = GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module)
        val javaPsiFacade = JavaPsiFacade.getInstance(project)

        val psiClass = javaPsiFacade.findClass(fullClassName, scope) ?: return

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
