package org.javamaster.mybatis.tools.annotator

import com.baomidou.plugin.idea.mybatisx.dom.model.IdDomElement
import com.baomidou.plugin.idea.mybatisx.dom.model.Select
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.database.psi.DbColumn
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.lang.java.beans.PropertyKind
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.SmartPointerManager
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlTag
import com.intellij.sql.psi.*
import com.intellij.util.xml.DomUtil
import org.javamaster.mybatis.tools.fix.AddColumnToResultMapQuickFix
import org.javamaster.mybatis.tools.fix.AddFieldToClassQuickFix
import org.javamaster.mybatis.tools.utils.StringUtil

/**
 * 对于 sql 的列名或别名,如果在 resultType 或 resultMap 中不存在,则标黄
 *
 * @author yudong
 */
class ColumnNameAnnotator : Annotator {

    override fun annotate(selectClause: PsiElement, holder: AnnotationHolder) {
        if (selectClause !is SqlSelectClause) return

        val project = selectClause.project

        if (DumbService.isDumb(project)) return

        val joinExpression = PsiTreeUtil.getParentOfType(selectClause, SqlJoinExpression::class.java)
        if (joinExpression != null) {
            return
        }

        if (PsiTreeUtil.getParentOfType(selectClause, SqlSelectClause::class.java) != null) {
            return
        }

        val injectionHost = InjectedLanguageManager.getInstance(project).getInjectionHost(selectClause) ?: return

        val selectDomElement = DomUtil.findDomElement(injectionHost, IdDomElement::class.java) ?: return

        if (selectDomElement !is Select) {
            return
        }

        val resultType = selectDomElement.resultType.value

        val existResultType = resultType != null && resultType.isWritable
        val fieldNames = if (existResultType) {
            cachedFieldNames(resultType)
        } else {
            null
        }

        val resultMap = selectDomElement.resultMap.value

        val existResultMap = resultMap != null && resultMap.isWritable
        val columnResolveList = if (existResultMap) {
            val xmlTags = PsiTreeUtil.findChildrenOfType(resultMap, XmlTag::class.java)

            xmlTags.mapNotNull {
                it.getAttribute("column")?.valueElement?.references?.lastOrNull()?.resolve()
            }
        } else {
            null
        }

        val expressions = PsiTreeUtil.getChildrenOfType(selectClause, SqlExpression::class.java) ?: return

        expressions.forEach {
            val columnName = it.name ?: return@forEach

            val nameElement = if (it is SqlReferenceExpression) {
                it.identifier
            } else if (it is SqlAsExpression) {
                it.nameElement
            } else {
                null
            }

            if (nameElement == null) {
                return@forEach
            }

            val references = it.references

            if (it !is SqlAsExpression) {
                val resolve = references.lastOrNull()?.resolve()
                if (resolve != it && resolve !is DbColumn) {
                    return@forEach
                }
            }

            when {
                existResultMap -> {
                    handleResultMap(resultMap, columnResolveList!!, columnName, it, nameElement, holder, project)
                }

                existResultType -> {
                    handleResultType(resultType, fieldNames!!, columnName, it, nameElement, holder, project)
                }
            }
        }
    }

    private fun handleResultType(
        resultType: PsiClass,
        fieldNames: Set<String>,
        columnName: String,
        expression: SqlExpression,
        nameElement: SqlIdentifier,
        holder: AnnotationHolder,
        project: Project,
    ) {
        val camelCaseName = StringUtil.toCamelCase(columnName)

        val matchesField = matchesField(fieldNames, camelCaseName, columnName)
        if (matchesField) {
            return
        }

        val manager = InspectionManager.getInstance(project)

        @Suppress("DialogTitleCapitalization")
        val descriptor = manager.createProblemDescriptor(
            nameElement, nameElement, "在 resultType 中新增字段", ProblemHighlightType.WARNING, false
        )

        val typeName = runCatching { expression.dasType.toDataType().typeName }.getOrNull() ?: "String"

        holder.newAnnotation(HighlightSeverity.WARNING, "在 resultType 中无法找到列 $columnName 对应的字段")
            .range(nameElement.textRange)
            .newLocalQuickFix(
                AddFieldToClassQuickFix(camelCaseName, resultType.qualifiedName ?: "", typeName),
                descriptor
            )
            .registerFix()
            .create()
    }

    private fun handleResultMap(
        resultMap: XmlTag,
        columnResolveList: List<PsiElement>,
        columnName: String,
        expression: SqlExpression,
        nameElement: SqlIdentifier,
        holder: AnnotationHolder,
        project: Project,
    ) {
        val resultXmlTag = columnResolveList.firstOrNull {
            it == expression
        }

        if (resultXmlTag != null) {
            return
        }

        val manager = InspectionManager.getInstance(project)

        @Suppress("DialogTitleCapitalization")
        val descriptor = manager.createProblemDescriptor(
            nameElement, nameElement, "在 resultMap 标签中新增 xml 标签", ProblemHighlightType.WARNING, false
        )

        val pointer = SmartPointerManager.getInstance(project).createSmartPsiElementPointer(resultMap)

        val camelCaseName = StringUtil.toCamelCase(columnName)

        holder.newAnnotation(HighlightSeverity.WARNING, "在 resultMap 标签中无法找到列 $columnName 对应的 xml 标签")
            .range(nameElement.textRange)
            .newLocalQuickFix(AddColumnToResultMapQuickFix(pointer, camelCaseName, columnName), descriptor)
            .registerFix()
            .create()
    }

    private fun matchesField(fieldNames: Set<String>, camel: String, raw: String): Boolean {
        return fieldNames.contains(camel)
                || fieldNames.contains(raw)
                || fieldNames.contains(PropertyKind.BOOLEAN_GETTER.prefix + StringUtil.capitalizeFirstWord2(camel))
    }

    private fun cachedFieldNames(psiClass: PsiClass): Set<String> {
        return CachedValuesManager.getCachedValue(psiClass) {
            CachedValueProvider.Result.create(
                psiClass.allFields.mapNotNull { it.name }.toSet(),
                PsiModificationTracker.MODIFICATION_COUNT,
            )
        }
    }

}
