package org.javamaster.mybatis.tools.fix

import com.intellij.codeInsight.intention.PriorityAction
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.intellij.pom.Navigatable
import com.intellij.psi.SmartPsiElementPointer
import com.intellij.psi.XmlElementFactory
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlTag

/**
 * @author yudong
 */
class AddColumnToResultMapQuickFix(
    @Suppress("ActionIsNotPreviewFriendly")
    val pointer: SmartPsiElementPointer<XmlTag?>,
    val fieldName: String,
    val columnName: String,
) : LocalQuickFix,
    PriorityAction {

    override fun getFamilyName(): String {
        @Suppress("DialogTitleCapitalization")
        return "在 resultMap 标签中新增 xml 标签"
    }

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val xmlResultMap = pointer.element ?: return

        var isIdTag: Boolean
        val tagText = if (name == "id" || name.contains("Id")) {
            isIdTag = true
            """<id property="$fieldName" column="$columnName"/>"""
        } else {
            isIdTag = false
            """<result property="$fieldName" column="$columnName"/>"""
        }

        val factory = XmlElementFactory.getInstance(project)

        val newTag = factory.createTagFromText(tagText)

        val subTags = PsiTreeUtil.getChildrenOfType(xmlResultMap, XmlTag::class.java)

        val newXmlTag = if (isIdTag || subTags == null) {
            xmlResultMap.addSubTag(newTag, true)
        } else {
            val tag = subTags.firstOrNull {
                val tagName = it.name
                tagName == "id" || tagName == "result"
            }
            if (tag != null) {
                xmlResultMap.addAfter(newTag, tag)
            } else {
                xmlResultMap.addSubTag(newTag, true)
            }
        }

        ((newXmlTag as XmlTag).attributes[1].valueElement as Navigatable).navigate(true)
    }

    override fun getPriority(): PriorityAction.Priority {
        return PriorityAction.Priority.HIGH
    }

}
