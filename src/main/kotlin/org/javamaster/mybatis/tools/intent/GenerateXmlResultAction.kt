package org.javamaster.mybatis.tools.intent

import com.baomidou.plugin.idea.mybatisx.util.MapperUtils
import com.intellij.codeInsight.intention.impl.BaseIntentionAction
import com.intellij.codeInspection.util.IntentionFamilyName
import com.intellij.codeInspection.util.IntentionName
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.module.ModuleUtil
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.InheritanceUtil
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.PsiUtil
import com.intellij.psi.xml.XmlTag
import org.javamaster.mybatis.tools.utils.TooltipUtils

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

        val element = getElement(editor, psiFile)

        val xmlTag = PsiTreeUtil.getParentOfType(element, XmlTag::class.java) ?: return false

        val tagName = xmlTag.name
        if (tagName != "resultMap" && tagName != "collection" && tagName != "association") {
            return false
        }

        val type = tryGetType(xmlTag)

        return type != null
    }

    override fun invoke(project: Project, editor: Editor, psiFile: PsiFile) {
        val element = getElement(editor, psiFile)!!
        val xmlTag = PsiTreeUtil.getParentOfType(element, XmlTag::class.java)!!

        val type = tryGetType(xmlTag) ?: return

        val module = ModuleUtil.findModuleForFile(psiFile) ?: return

        val scope = GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module)

        val javaPsiFacade = JavaPsiFacade.getInstance(project)
        val psiClass = javaPsiFacade.findClass(type, scope)
        if (psiClass == null) {
            TooltipUtils.showTip("无法解析 $type !", project)
            return
        }

        var noId = true
        var first = true

        val resultStr = psiClass.allFields
            .map {
                val pair = handleCollectionOrAssociation(it)
                if (pair != null) {
                    return@map pair
                }

                val spaceStr = if (first) {
                    ""
                } else {
                    if (xmlTag.name == "collection" || xmlTag.name == "association") {
                        "            "
                    } else {
                        "        "
                    }
                }

                first = false

                val name = it.name
                val str = if ((name == "id" || name.contains("Id")) && noId) {
                    noId = false
                    """$spaceStr<id property="$name" column="$name"/>"""
                } else {
                    """$spaceStr<result property="$name" column="$name"/>"""
                }
                Pair(str, 1)
            }
            .sortedBy { it.second }
            .joinToString("\n") { it.first }

        val caretModel = editor.caretModel
        val position = caretModel.offset
        val document = PsiDocumentManager.getInstance(project).getDocument(psiFile)!!
        document.insertString(position, resultStr)
    }

    private fun handleCollectionOrAssociation(psiField: PsiField): Pair<String, Int>? {
        val fieldType = psiField.type
        val name = psiField.name
        if (fieldType !is PsiClassType) return null

        val fieldPsiClass = fieldType.resolve() ?: return null

        val isCollection = InheritanceUtil.isInheritor(fieldPsiClass, "java.util.Collection")
        if (isCollection) {
            val typeParameters = fieldType.parameters
            if (typeParameters.isEmpty()) {
                return null
            }

            val typePsiType = typeParameters[0] as PsiClassType
            val typePsiClass = typePsiType.resolve() ?: return null
            val str = """
        <collection property="$name" ofType="${typePsiClass.qualifiedName}">
        
        </collection>"""
            return Pair(str, 3)
        } else {

            val packageName = PsiUtil.getPackageName(fieldPsiClass) ?: return null
            if (packageName.startsWith("java")) {
                return null
            }

            val str = """
        <association property="$name" javaType="${fieldPsiClass.qualifiedName}">
            
        </association>"""
            return Pair(str, 2)
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

}
