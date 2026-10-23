package org.javamaster.mybatis.tools.utils

import com.baomidou.plugin.idea.mybatisx.dom.model.IdDomElement
import com.baomidou.plugin.idea.mybatisx.dom.model.Mapper
import com.baomidou.plugin.idea.mybatisx.dom.model.Sql
import com.baomidou.plugin.idea.mybatisx.util.MapperUtils
import com.intellij.psi.PsiMethod
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlTag
import com.intellij.util.xml.DomUtil

/**
 * @author yudong
 */
object MapperUtil {

    fun getSqlPsiMethod(sql: Sql): PsiMethod? {
        val id = sql.xmlTag?.getAttributeValue("id") ?: return null

        val mapper = MapperUtils.getMapper(sql)

        val xmlTag = PsiTreeUtil.findChildrenOfType(mapper.xmlElement, XmlTag::class.java)
            .firstOrNull { it.name == "include" && it.getAttributeValue("refid") == id } ?: return null

        val domElement = DomUtil.findDomElement(xmlTag, IdDomElement::class.java)

        return domElement?.id?.value as? PsiMethod
    }

    fun getMapper(xmlTag: XmlTag): Mapper? {
        val firstChild = xmlTag.firstChild ?: return null

        val domElement = DomUtil.findDomElement(firstChild, IdDomElement::class.java) ?: return null

        return MapperUtils.getMapper(domElement)
    }

}
