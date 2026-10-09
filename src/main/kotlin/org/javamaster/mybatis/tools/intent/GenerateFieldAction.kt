package org.javamaster.mybatis.tools.intent

import com.intellij.codeInsight.intention.impl.BaseIntentionAction
import com.intellij.codeInspection.util.IntentionName
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.sql.psi.SqlSelectClause
import org.javamaster.mybatis.tools.utils.StringUtil
import java.awt.datatransfer.StringSelection

/**
 * @author yudong
 */
class GenerateFieldAction : BaseIntentionAction() {
    override fun getText(): @IntentionName String {
        @Suppress("DialogTitleCapitalization")
        return "生成Java类字段定义"
    }

    override fun getFamilyName(): String {
        return text
    }

    override fun isAvailable(project: Project, editor: Editor, file: PsiFile): Boolean {
        val element = getElement(editor, file)

        PsiTreeUtil.getParentOfType(element, SqlSelectClause::class.java) ?: return false

        return true
    }

    override fun invoke(project: Project, editor: Editor, file: PsiFile) {
        val element = getElement(editor, file) ?: return

        val selectClause = PsiTreeUtil.getParentOfType(element, SqlSelectClause::class.java) ?: return

        val expressions = selectClause.expressions
        val fieldsStr = expressions.map {
            val name = it.name
            val fieldName = StringUtil.toCamelCase(name)
            val dataType = it.dasType.toDataType()
            val typeName = dataType.typeName

            return@map if (typeName.contains("bigint")) {
                "private Long $fieldName;"
            } else if (typeName.contains("int")) {
                "private Integer $fieldName;"
            } else if (typeName.contains("date") || typeName.contains("time")) {
                "private Date $fieldName;"
            } else if (typeName.contains("char") || typeName.contains("text")) {
                "private String $fieldName;"
            } else {
                "private $typeName $fieldName;"
            }
        }.joinToString("\n")

        CopyPasteManager.getInstance().setContents(StringSelection(fieldsStr))
//        NotifyUtil.notifyBalloonInfo(project, "已生成 field 字段并放入到剪切板!")
    }

    private fun getElement(editor: Editor, file: PsiFile): PsiElement? {
        val caretModel = editor.caretModel
        val position = caretModel.offset
        return file.findElementAt(position)
    }

    override fun startInWriteAction(): Boolean {
        return false
    }
}
