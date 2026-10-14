package org.javamaster.mybatis.tools.intent

import com.baomidou.plugin.idea.mybatisx.dom.model.IdDomElement
import com.baomidou.plugin.idea.mybatisx.util.DomUtils
import com.intellij.codeInsight.intention.impl.BaseIntentionAction
import com.intellij.codeInsight.intention.preview.IntentionPreviewUtils
import com.intellij.codeInspection.util.IntentionName
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.module.ModuleUtil
import com.intellij.openapi.project.Project
import com.intellij.pom.Navigatable
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiMethod
import com.intellij.psi.XmlElementFactory
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.psi.xml.XmlTag
import com.intellij.util.xml.DomUtil
import org.javamaster.mybatis.tools.utils.*

/**
 * @author yudong
 */
class ConvertResultTypeToResultMapAction : BaseIntentionAction() {

    override fun getText(): @IntentionName String {
        @Suppress("DialogTitleCapitalization")
        return "将 resultType 转换为 resultMap"
    }

    override fun getFamilyName(): String {
        return text
    }

    override fun isAvailable(project: Project, editor: Editor, file: PsiFile): Boolean {
        if (!DomUtils.isMybatisFile(file)) {
            return false
        }

        return getXmlElement(editor, file) != null
    }

    override fun invoke(project: Project, editor: Editor, file: PsiFile) {
        if (IntentionPreviewUtils.isIntentionPreviewActive()) {
            return
        }

        val attribute = getXmlElement(editor, file) ?: return

        val fullClassName = attribute.value ?: return

        val selectXmlTag = attribute.parent
        val mapperXmlTag = selectXmlTag.parent as XmlTag

        val domElement = DomUtil.findDomElement(attribute, IdDomElement::class.java)

        val psiMethod = domElement?.id?.value as? PsiMethod?

        val module = if (psiMethod != null) {
            ModuleUtil.findModuleForPsiElement(psiMethod)
        } else {
            null
        }

        val psiClass = PsiUtils.resolvePsiClass(fullClassName, module, project)
        if (psiClass == null) {
            TooltipUtils.showTip("无法解析 $fullClassName !", project)
            return
        }

        if (psiClass.qualifiedName?.startsWith("java") == true) {
            TooltipUtils.showTip("无法转换 $fullClassName !", project)
            return
        }

        val factory = XmlElementFactory.getInstance(project)

        val resultMapTag = factory.createTagFromText(
            """<resultMap id="selectResultMap" type="$fullClassName"></resultMap>"""
        )

        val columnNames = if (DatabasePluginUtils.databasePluginAvailable) {
            XmlSqlUtils.collectColumnNames(selectXmlTag, project)
        } else {
            null
        }

        val resultMapContents = GenerateUtils.generateResultMapContents(psiClass, columnNames, project)

        resultMapContents.forEach {
            resultMapTag.addSubTag(it, false)
        }

        val newTag = mapperXmlTag.addBefore(resultMapTag, selectXmlTag)

        attribute.delete()
        selectXmlTag.setAttribute("resultMap", "selectResultMap")

        (newTag as Navigatable).navigate(true)
    }

    private fun getXmlElement(editor: Editor, file: PsiFile): XmlAttribute? {
        val caretModel = editor.caretModel
        val position = caretModel.offset

        val psiElement = file.findElementAt(position)
        if (psiElement == null) {
            return null
        }

        val parent = psiElement.parent

        val attribute = parent as? XmlAttribute
            ?: if (parent is XmlAttributeValue) {
                parent.parent as XmlAttribute
            } else {
                return null
            }

        if (attribute.name != "resultType") {
            return null
        }

        return attribute
    }

    override fun startInWriteAction(): Boolean {
        return true
    }
}
