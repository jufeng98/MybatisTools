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
import org.javamaster.mybatis.tools.utils.XmlSqlUtils.tryGetType

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

        val domElement = DomUtil.getDomElement(xmlTag)

        val type = tryGetType(domElement)

        return type != null
    }

    override fun invoke(project: Project, editor: Editor, psiFile: PsiFile) {
        val element = getElement(editor, psiFile) ?: return

        val xmlTag = PsiTreeUtil.getParentOfType(element, XmlTag::class.java) ?: return

        val domElement = DomUtil.getDomElement(xmlTag)

        val type = tryGetType(domElement) ?: return

        val resultMapId = getResultMapId(xmlTag)

        val idDomElement = DomUtil.findDomElement(element, IdDomElement::class.java)

        val psiMethod = idDomElement?.id?.value as? PsiMethod?

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
            if (idDomElement != null) {
                val mapper = MapperUtils.getMapper(idDomElement)

                val firstSelect = mapper.selects
                    .firstOrNull { it.xmlTag?.getAttributeValue("resultMap") == resultMapId }

                val firstSelectXmlTag = firstSelect?.xmlTag

                XmlSqlUtils.collectColumnNames(firstSelectXmlTag, project)
            } else {
                emptySet()
            }
        } else {
            emptySet()
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

    override fun startInWriteAction(): Boolean {
        return true
    }
}
