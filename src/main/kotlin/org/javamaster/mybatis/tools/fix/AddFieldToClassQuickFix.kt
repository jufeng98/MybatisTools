package org.javamaster.mybatis.tools.fix

import com.intellij.codeInsight.intention.PriorityAction
import com.intellij.codeInsight.intention.preview.IntentionPreviewUtils
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.pom.Navigatable
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElementFactory
import com.intellij.psi.SmartPsiElementPointer
import com.intellij.sql.psi.SqlExpression
import com.intellij.util.application
import org.javamaster.mybatis.tools.utils.GenerateUtils

/**
 * @author yudong
 */
@Suppress("ActionIsNotPreviewFriendly")
class AddFieldToClassQuickFix(
    val classPointer: SmartPsiElementPointer<PsiClass>,
    val fieldName: String,
    val expressionPointer: SmartPsiElementPointer<SqlExpression>,
) : LocalQuickFix,
    PriorityAction {

    override fun getFamilyName(): String {
        @Suppress("DialogTitleCapitalization")
        return "在 resultType 的类中新增字段"
    }

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        if (IntentionPreviewUtils.isIntentionPreviewActive()) {
            return
        }

        if (!application.isDispatchThread) {
            return
        }

        val psiClass = classPointer.element ?: return

        FileEditorManager.getInstance(project).openFile(psiClass.containingFile.virtualFile, true)

        val typeName = runCatching {
            expressionPointer.element?.dasType?.toDataType()?.typeName
        }.getOrNull() ?: "String"

        val fieldText = GenerateUtils.generateJavaField(fieldName, typeName).trim()

        val elementFactory = PsiElementFactory.getInstance(project)
        val field = elementFactory.createFieldFromText(fieldText, psiClass)

        val element = psiClass.add(field)

        (element as Navigatable).navigate(true)
    }

    override fun getPriority(): PriorityAction.Priority {
        return PriorityAction.Priority.NORMAL
    }

}
