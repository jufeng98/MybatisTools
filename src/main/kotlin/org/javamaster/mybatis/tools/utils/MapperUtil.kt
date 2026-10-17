package org.javamaster.mybatis.tools.utils

import com.baomidou.plugin.idea.mybatisx.dom.model.IdDomElement
import com.baomidou.plugin.idea.mybatisx.dom.model.Mapper
import com.baomidou.plugin.idea.mybatisx.dom.model.Sql
import com.baomidou.plugin.idea.mybatisx.util.MapperUtils
import com.intellij.psi.PsiMethod
import com.intellij.psi.xml.XmlTag
import com.intellij.util.xml.DomUtil

/**
 * @author yudong
 */
object MapperUtil {

    fun getSqlPsiMethod(sql: Sql): PsiMethod? {
        val id = sql.xmlTag?.getAttributeValue("id") ?: return null

        val mapper = MapperUtils.getMapper(sql)

        val select = mapper.selects
            .firstOrNull { it.includes.any { it.refId.stringValue == id } } ?: return null

        return select.id.value as? PsiMethod
    }

    fun getMapper(xmlTag: XmlTag): Mapper? {
        val firstChild = xmlTag.firstChild ?: return null

        val domElement = DomUtil.findDomElement(firstChild, IdDomElement::class.java) ?: return null

        return MapperUtils.getMapper(domElement)
    }

}
