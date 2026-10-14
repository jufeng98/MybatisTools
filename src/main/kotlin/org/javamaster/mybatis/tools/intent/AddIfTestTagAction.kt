package org.javamaster.mybatis.tools.intent

import com.baomidou.plugin.idea.mybatisx.util.DomUtils
import com.intellij.codeInsight.intention.impl.BaseIntentionAction
import com.intellij.codeInspection.util.IntentionName
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.lang.ognl.psi.OgnlReferenceExpression
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiField
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiParameter
import com.intellij.psi.util.InheritanceUtil
import com.intellij.psi.xml.XmlText
import org.apache.commons.lang3.StringUtils
import org.javamaster.mybatis.tools.utils.PsiUtils
import org.javamaster.mybatis.tools.utils.StringUtil

/**
 * @author yudong
 */
class AddIfTestTagAction : BaseIntentionAction() {

    override fun getText(): @IntentionName String {
        @Suppress("DialogTitleCapitalization")
        return "添加 if test 条件判断标签"
    }

    override fun getFamilyName(): String {
        return text
    }

    override fun isAvailable(project: Project, editor: Editor, file: PsiFile): Boolean {
        if (!DomUtils.isMybatisFile(file)) {
            return false
        }

        return getXmlElement(editor, file, project) != null
    }

    override fun invoke(project: Project, editor: Editor, file: PsiFile) {
        val pair = getXmlElement(editor, file, project) ?: return

        val xmlText = pair.first
        val referenceExpression = pair.second

        val fieldName = referenceExpression.text

        val references = referenceExpression.references
        val ifTagStartText = if (references.isNotEmpty()) {
            val resolve = references.last().resolve()
            if (resolve is PsiParameter || resolve is PsiField) {
                val psiClass = PsiUtils.resolvePsiType(resolve.type)
                val qualifiedName = psiClass?.qualifiedName
                if (qualifiedName == "java.lang.String") {
                    """<if test="$fieldName != null and $fieldName != ''">"""
                } else if (InheritanceUtil.isInheritor(psiClass, "java.util.Collection")) {
                    """<if test="$fieldName != null and $fieldName.size() > 0">"""
                } else {
                    """<if test="$fieldName != null">"""
                }
            } else {
                """<if test="$fieldName != null">"""
            }
        } else {
            """<if test="$fieldName != null">"""
        }

        val offset = InjectedLanguageManager.getInstance(project)
            .injectedToHost(referenceExpression, xmlText.textRange.endOffset)

        val newOffset = offset - xmlText.textOffset

        // 找到前后换行符的位置
        val xmlTextStr = xmlText.text
        val start = xmlTextStr.lastIndexOf("\n", newOffset)
        if (start == -1) {
            return
        }

        val end = xmlTextStr.indexOf("\n", newOffset)
        if (end == -1) {
            return
        }

        val startOffset = xmlText.textRange.startOffset

        var count = StringUtil.countStartSpace(xmlTextStr.substring(start + 1))
        if (count > 4) {
            count -= 4
        }

        val space = StringUtils.repeat(' ', count)

        val doc = editor.document
        val ifTagStart = "\n$space$ifTagStartText"
        doc.insertString(startOffset + start, ifTagStart)

        val ifTagEnd = "\n$space</if>\n"
        doc.insertString(startOffset + end + ifTagStart.length, ifTagEnd)
    }

    private fun getXmlElement(
        editor: Editor,
        file: PsiFile,
        project: Project,
    ): Pair<XmlText, OgnlReferenceExpression>? {
        val caretModel = editor.caretModel
        val position = caretModel.offset

        val psiElement = file.findElementAt(position)
        if (psiElement == null) {
            return null
        }

        val xmlText = psiElement.parent
        if (xmlText !is XmlText) {
            return null
        }

        val injectedLanguageManager = InjectedLanguageManager.getInstance(project)

        val injectedElement = injectedLanguageManager.findInjectedElementAt(file, position) ?: return null

        val ognlReferenceExpression = injectedElement.parent
        if (ognlReferenceExpression !is OgnlReferenceExpression) {
            return null
        }

        return Pair(xmlText, ognlReferenceExpression)
    }

    override fun startInWriteAction(): Boolean {
        return true
    }
}
