package org.javamaster.mybatis.tools.intent

import com.baomidou.plugin.idea.mybatisx.dom.model.IdDomElement
import com.baomidou.plugin.idea.mybatisx.util.MapperUtils
import com.baomidou.plugin.idea.mybatisx.util.MapperUtils.getResultMapId
import com.intellij.codeInsight.intention.impl.BaseIntentionAction
import com.intellij.codeInspection.util.IntentionFamilyName
import com.intellij.codeInspection.util.IntentionName
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.module.ModuleUtil
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiMethod
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlTag
import com.intellij.util.xml.DomUtil
import org.javamaster.mybatis.tools.utils.*

/**
 * @author yudong
 */
class GenerateXmlResultAction : BaseIntentionAction() {

    override fun getText(): @IntentionName String {
        @Suppress("DialogTitleCapitalization")
        return "生成 MyBatis xml result 标签"
    }

    override fun getFamilyName(): @IntentionFamilyName String {
        return text
    }

    override fun isAvailable(project: Project, editor: Editor, psiFile: PsiFile): Boolean {
        if (!MapperUtils.isElementWithinMybatisFile(psiFile)) {
            return false
        }

        val element = getElement(editor, psiFile) ?: return false

        val xmlTag = PsiTreeUtil.getParentOfType(element, XmlTag::class.java) ?: return false

        val tagName = xmlTag.name
        if (tagName != "resultMap" && tagName != "collection" && tagName != "association") {
            return false
        }

        val type = tryGetType(xmlTag)

        return type != null
    }

    override fun invoke(project: Project, editor: Editor, psiFile: PsiFile) {
        val element = getElement(editor, psiFile) ?: return

        val xmlTag = PsiTreeUtil.getParentOfType(element, XmlTag::class.java) ?: return

        val type = tryGetType(xmlTag) ?: return

        val resultMapId = getResultMapId(xmlTag)

        val domElement = DomUtil.findDomElement(element, IdDomElement::class.java)

        val psiMethod = domElement?.id?.value as? PsiMethod?

        val module = if (psiMethod != null) {
            ModuleUtil.findModuleForPsiElement(psiMethod)
        } else {
            null
        }

        val psiClass = PsiUtils.resolvePsiClass(type, module, project)
        if (psiClass == null) {
            TooltipUtils.showTip("无法解析 $type !", project)
            return
        }

        val columnNames = if (DatabasePluginUtils.databasePluginAvailable) {
            if (domElement != null) {
                val mapper = MapperUtils.getMapper(domElement)

                val firstSelect = mapper.selects
                    .firstOrNull { it.xmlTag?.getAttributeValue("resultMap") == resultMapId }

                val firstSelectXmlTag = firstSelect?.xmlTag

                XmlSqlUtils.collectColumnNames(firstSelectXmlTag, project)
            } else {
                null
            }
        } else {
            null
        }

        val resultMapContents = GenerateUtils.generateResultMapContents(psiClass, columnNames, project)

        resultMapContents.forEach {
            xmlTag.addSubTag(it, false)
        }
    }

    private fun getElement(editor: Editor, file: PsiFile): PsiElement? {
        val caretModel = editor.caretModel
        val position = caretModel.offset
        return file.findElementAt(position)
    }

    private fun tryGetType(xmlTag: XmlTag): String? {
        var type = xmlTag.getAttributeValue("type")
        if (!type.isNullOrBlank()) {
            return type
        }

        type = xmlTag.getAttributeValue("ofType")
        if (!type.isNullOrBlank()) {
            return type
        }

        type = xmlTag.getAttributeValue("javaType")
        if (!type.isNullOrBlank()) {
            return type
        }

        return null
    }

    override fun startInWriteAction(): Boolean {
        return true
    }
}
