package org.javamaster.mybatis.tools.intent

import com.baomidou.plugin.idea.mybatisx.annotation.Annotation
import com.baomidou.plugin.idea.mybatisx.dom.model.IdDomElement
import com.baomidou.plugin.idea.mybatisx.dom.model.Sql
import com.baomidou.plugin.idea.mybatisx.util.DomUtils
import com.intellij.codeInsight.intention.impl.BaseIntentionAction
import com.intellij.codeInspection.util.IntentionName
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiMethod
import com.intellij.psi.impl.source.PsiClassReferenceType
import com.intellij.psi.util.InheritanceUtil
import com.intellij.psi.xml.XmlText
import com.intellij.util.xml.DomUtil
import org.javamaster.mybatis.tools.utils.MapperUtil
import org.javamaster.mybatis.tools.utils.PsiUtils
import org.javamaster.mybatis.tools.utils.StringUtil

/**
 * @author yudong
 */
class AddForeachTagAction : BaseIntentionAction() {

    override fun getText(): @IntentionName String {
        @Suppress("DialogTitleCapitalization")
        return "添加 foreach 标签"
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
        val xmlText = getXmlElement(editor, file) ?: return

        val domElement = DomUtil.findDomElement(xmlText, IdDomElement::class.java) ?: return

        val psiMethod = if (domElement is Sql) {
            MapperUtil.getSqlPsiMethod(domElement)
        } else {
            domElement.id.value as? PsiMethod
        } ?: return


        val pair = calName(psiMethod, project)

        var name = pair.first
        var nameClass = pair.second

        val itemName = calItemName(name, nameClass)

        val offset = editor.caretModel.primaryCaret.offset

        val ifTagStartText =
            """
        <foreach item="$itemName" index="index" collection="$name" open="(" separator="," close=")">
            #{$itemName}
        </foreach>"""

        val doc = editor.document
        doc.insertString(offset, ifTagStartText)
    }

    private fun getXmlElement(
        editor: Editor,
        file: PsiFile,
    ): XmlText? {
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

        return xmlText
    }

    private fun calName(psiMethod: PsiMethod, project: Project): Pair<String, PsiClass?> {
        val helper = JavaPsiFacade.getInstance(project).constantEvaluationHelper

        val parameterList = psiMethod.parameterList
        val parameters = parameterList.parameters

        var name: String? = null
        var nameClass: PsiClass? = null

        for (psiParameter in parameters) {
            val type = psiParameter.type
            if (!InheritanceUtil.isInheritor(type, "java.util.Collection")) continue

            nameClass = PsiUtils.resolvePsiType(type)

            val annotation = psiParameter.getAnnotation(Annotation.PARAM.qualifiedName)
            val paramAnnotationValue = annotation?.findAttributeValue("value")
            val paramValue = helper.computeConstantExpression(paramAnnotationValue) as String?

            name = paramValue ?: psiParameter.name

            break
        }

        if (name == null) {
            for (psiParameter in parameters) {
                val psiClass = PsiUtils.resolvePsiType(psiParameter.type) ?: continue

                for (psiField in psiClass.allFields) {
                    val type = psiField.type
                    if (!InheritanceUtil.isInheritor(type, "java.util.Collection")) continue

                    nameClass = PsiUtils.resolvePsiType(type)

                    name = psiField.name

                    break
                }
            }
        }

        if (name == null) {
            name = "items"
        }

        return Pair(name, nameClass)
    }

    private fun calItemName(name: String, nameClass: PsiClass?): String {
        return if (name.endsWith("s")) {
            name.substring(0, name.length - 1)
        } else if (name.endsWith("List")) {
            name.substring(0, name.length - 4)
        } else {
            if (nameClass != null) {
                val genericParameters = (nameClass as PsiClassReferenceType).parameters
                if (genericParameters.size == 0) {
                    "item"
                } else {
                    val psiClass = PsiUtils.resolvePsiType(genericParameters[0])
                    if (psiClass != null && psiClass.name != null) {
                        StringUtil.toCamelCase(psiClass.name)
                    } else {
                        name.substring(0, name.length - 1)
                    }
                }
            } else {
                "item"
            }
        }
    }

    override fun startInWriteAction(): Boolean {
        return true
    }
}
