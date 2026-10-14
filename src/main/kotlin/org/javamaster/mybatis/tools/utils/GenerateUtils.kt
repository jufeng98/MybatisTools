package org.javamaster.mybatis.tools.utils

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiField
import com.intellij.psi.XmlElementFactory
import com.intellij.psi.util.InheritanceUtil
import com.intellij.psi.util.PsiUtil
import com.intellij.psi.xml.XmlTag

object GenerateUtils {

    fun generateResultMapContents(psiClass: PsiClass, columnNames: Set<String>?, project: Project): List<XmlTag> {
        var noId = true

        val factory = XmlElementFactory.getInstance(project)

        return psiClass.allFields
            .filter { it.modifierList?.text?.contains("static") == false }
            .map {
                val pair = handleCollectionOrAssociation(it, factory)
                if (pair != null) {
                    return@map pair
                }

                val name = it.name

                val columnName = if (columnNames != null) {
                    if (columnNames.contains(name)) {
                        name
                    } else {
                        val underlineName = StringUtil.camelToUnderline(name)
                        if (columnNames.contains(underlineName)) {
                            underlineName
                        } else {
                            name
                        }
                    }
                } else {
                    name
                }

                if ((name == "id" || name.contains("Id")) && noId) {
                    noId = false
                    val tag = factory.createTagFromText("""<id property="$name" column="$columnName"/>""")
                    Pair(tag, 1)
                } else {
                    val tag = factory.createTagFromText("""<result property="$name" column="$columnName"/>""")
                    Pair(tag, 2)
                }
            }
            .sortedBy { it.second }
            .map { it.first }
    }

    private fun handleCollectionOrAssociation(psiField: PsiField, factory: XmlElementFactory): Pair<XmlTag, Int>? {
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

            val tag = factory.createTagFromText(
                """
                    <collection property="$name" ofType="${typePsiClass.qualifiedName}">
                    
                    </collection>
                """.trim()
            )
            return Pair(tag, 4)
        } else {
            val packageName = PsiUtil.getPackageName(fieldPsiClass) ?: return null

            if (packageName.startsWith("java")) {
                return null
            }

            val tag = factory.createTagFromText(
                """
                    <association property="$name" javaType="${fieldPsiClass.qualifiedName}">
                        
                    </association>
                """.trim()
            )
            return Pair(tag, 3)
        }
    }

    fun generateJavaField(name: String, typeName: String): String {
        val fieldName = StringUtil.toCamelCase(name)

        return if (typeName.contains("bigint")) {
            "    private Long $fieldName;"
        } else if (typeName.contains("int")) {
            "    private Integer $fieldName;"
        } else if (typeName.contains("date") || typeName.contains("time")) {
            "    private Date $fieldName;"
        } else if (typeName.contains("char") || typeName.contains("text")) {
            "    private String $fieldName;"
        } else if (typeName.contains("float") || typeName.contains("double")
            || typeName.contains("real") || typeName.contains("decimal") || typeName.contains("numeric")
        ) {
            "    private Double $fieldName;"
        } else {
            "    private String $fieldName;"
        }
    }

}
