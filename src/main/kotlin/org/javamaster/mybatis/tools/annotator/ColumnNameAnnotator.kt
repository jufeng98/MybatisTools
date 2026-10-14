package org.javamaster.mybatis.tools.annotator

import com.baomidou.plugin.idea.mybatisx.dom.model.IdDomElement
import com.baomidou.plugin.idea.mybatisx.util.MapperUtils
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.database.psi.DbColumn
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.module.ModuleUtil
import com.intellij.openapi.project.Project
import com.intellij.patterns.PsiJavaPatterns.psiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiLanguageInjectionHost
import com.intellij.psi.SmartPointerManager
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlTag
import com.intellij.sql.psi.*
import com.intellij.util.xml.DomUtil
import org.javamaster.mybatis.tools.fix.AddColumnToResultMapQuickFix
import org.javamaster.mybatis.tools.fix.AddFieldToClassQuickFix
import org.javamaster.mybatis.tools.utils.PsiUtils
import org.javamaster.mybatis.tools.utils.StringUtil

/**
 * @author yudong
 */
class ColumnNameAnnotator : Annotator {

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        val project = element.project

        if (element !is SqlExpression) {
            return
        }

        if (element.parent is SqlAsExpression) {
            return
        }

        val injectionHost = InjectedLanguageManager.getInstance(project).getInjectionHost(element)
        if (injectionHost !is PsiElement) {
            return
        }

        val xmlTag = tryGetSelectTag(element, injectionHost) ?: return

        val resultType = xmlTag.getAttributeValue("resultType")
        if (resultType != null) {
            handleResultType(resultType, element, holder, project)
        }

        val resultMap = xmlTag.getAttributeValue("resultMap")
        if (resultMap != null) {
            handleResultMap(resultMap, element, injectionHost, holder, project)
        }
    }

    private fun tryGetSelectTag(
        element: SqlExpression,
        injectionHost: PsiLanguageInjectionHost,
    ): XmlTag? {
        if (!MapperUtils.isElementWithinMybatisFile(injectionHost)) {
            return null
        }

        val xmlTag = injectionHost.parent
        if (xmlTag !is XmlTag) {
            return null
        }

        if (xmlTag.name != "select") {
            return null
        }

        val references = element.references
        if (references.isEmpty()) {
            return null
        }

        if (element !is SqlAsExpression && references[0].resolve() !is DbColumn) {
            return null
        }

        val selectClause = PsiTreeUtil.getParentOfType(element, SqlSelectClause::class.java) ?: return null

        val joinExpression = PsiTreeUtil.getParentOfType(selectClause, SqlJoinExpression::class.java)
        if (joinExpression != null) {
            return null
        }

        return xmlTag
    }

    private fun handleResultType(
        resultTypeStr: String,
        element: SqlExpression,
        holder: AnnotationHolder,
        project: Project,
    ) {
        val module = ModuleUtil.findModuleForFile(element.containingFile)

        val psiClass = PsiUtils.resolvePsiClass(resultTypeStr, module, project) ?: return

        if (!psiClass.isWritable) {
            return
        }

        PsiTreeUtil.getParentOfType(element, SqlSelectClause::class.java) ?: return

        val columnName = element.name ?: return

        val fieldName = StringUtil.toCamelCase(columnName)

        val psiField = psiClass.findFieldByName(fieldName, true)
        if (psiField != null) {
            return
        }

        val nameElement = if (element is SqlAsExpression) {
            element.nameElement ?: return
        } else if (element is SqlReferenceExpression) {
            element.identifier ?: return
        } else {
            return
        }

        val manager = InspectionManager.getInstance(project)

        @Suppress("DialogTitleCapitalization")
        val descriptor = manager.createProblemDescriptor(
            nameElement, nameElement, "在 resultType 中新增字段", ProblemHighlightType.WARNING, false
        )

        val typeName = element.dasType.toDataType().typeName

        holder.newAnnotation(HighlightSeverity.WARNING, "在 resultType 中无法找到列 $columnName 对应的字段")
            .range(nameElement.textRange)
            .newLocalQuickFix(AddFieldToClassQuickFix(fieldName, resultTypeStr, typeName), descriptor)
            .registerFix()
            .create()
    }

    private fun handleResultMap(
        resultMapId: String,
        element: SqlExpression,
        injectionHost: PsiLanguageInjectionHost,
        holder: AnnotationHolder,
        project: Project,
    ) {
        val domElement = DomUtil.findDomElement(injectionHost, IdDomElement::class.java) ?: return

        val mapper = MapperUtils.getMapper(domElement)

        val resultMap = mapper.resultMaps
            .firstOrNull { it.id.xmlAttributeValue?.value == resultMapId } ?: return

        val xmlResultMap = resultMap.xmlTag ?: return

        val xmlTags = PsiTreeUtil.findChildrenOfType(xmlResultMap, XmlTag::class.java)

        val columnName = element.name ?: return

        val resultXmlTag = xmlTags.firstOrNull {
            val attributeValue = it.getAttributeValue("column")
            attributeValue == columnName
        }

        if (resultXmlTag != null) {
            return
        }

        val nameElement = if (element is SqlAsExpression) {
            element.nameElement ?: return
        } else if (element is SqlReferenceExpression) {
            element.identifier ?: return
        } else {
            return
        }

        val manager = InspectionManager.getInstance(project)

        @Suppress("DialogTitleCapitalization")
        val descriptor = manager.createProblemDescriptor(
            nameElement, nameElement, "在 resultMap 标签中新增 xml 标签", ProblemHighlightType.WARNING, false
        )

        val pointer = SmartPointerManager.getInstance(element.project).createSmartPsiElementPointer(xmlResultMap)

        val fieldName = StringUtil.toCamelCase(columnName)

        holder.newAnnotation(HighlightSeverity.WARNING, "在 resultMap 标签中无法找到列 $columnName 对应的 xml 标签")
            .range(nameElement.textRange)
            .newLocalQuickFix(AddColumnToResultMapQuickFix(pointer, fieldName, columnName), descriptor)
            .registerFix()
            .create()
    }

}
