package org.javamaster.mybatis.tools.intent

import com.baomidou.plugin.idea.mybatisx.util.DomUtils
import com.intellij.codeInsight.intention.impl.BaseIntentionAction
import com.intellij.codeInspection.util.IntentionName
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.sql.psi.SqlElement
import com.intellij.sql.psi.SqlSelectClause
import com.intellij.testFramework.LightVirtualFile
import org.javamaster.mybatis.tools.utils.GenerateUtils

/**
 * @author yudong
 */
class GenerateFieldFromSqlAction : BaseIntentionAction() {

    override fun getText(): @IntentionName String {
        @Suppress("DialogTitleCapitalization")
        return "根据 SQL 的列名或列别名生成 Java 类字段"
    }

    override fun getFamilyName(): String {
        return text
    }

    override fun isAvailable(project: Project, editor: Editor, file: PsiFile): Boolean {
        if (!DomUtils.isMybatisFile(file)) {
            return false
        }

        val element = getSqlElement(editor, file, project) ?: return false

        PsiTreeUtil.getParentOfType(element, SqlSelectClause::class.java) ?: return false

        return true
    }

    override fun invoke(project: Project, editor: Editor, file: PsiFile) {
        val element = getSqlElement(editor, file, project) ?: return

        val selectClause = PsiTreeUtil.getParentOfType(element, SqlSelectClause::class.java) ?: return

        val fieldsStr = selectClause.expressions
            .mapNotNull {
                val name = it.name ?: return@mapNotNull null

                val dataType = it.dasType.toDataType()

                GenerateUtils.generateJavaField(name, dataType.typeName)
            }
            .joinToString("\n")

        val fileEditorManager = FileEditorManager.getInstance(project)
        val virtualFile = LightVirtualFile(
            "FieldNameFromSql.java",
            """
import java.util.*;
                
public class FieldNameFromSql {
$fieldsStr
}
            """.trimIndent()
        )

        fileEditorManager.openFile(virtualFile, true)
    }

    private fun getSqlElement(editor: Editor, file: PsiFile, project: Project): PsiElement? {
        val caretModel = editor.caretModel
        val position = caretModel.offset

        val psiElement = file.findElementAt(position)
        if (psiElement != null) {
            val parent = psiElement.parent
            if (parent is SqlElement) {
                return parent
            }
        }

        val injectedLanguageManager = InjectedLanguageManager.getInstance(project)

        return injectedLanguageManager.findInjectedElementAt(file, position)
    }

    override fun startInWriteAction(): Boolean {
        return false
    }
}
