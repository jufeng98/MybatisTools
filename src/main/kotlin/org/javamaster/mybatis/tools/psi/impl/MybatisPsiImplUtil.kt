package org.javamaster.mybatis.tools.psi.impl

import com.intellij.psi.PsiReference
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry
import org.javamaster.mybatis.tools.psi.MybatisAttrName
import org.javamaster.mybatis.tools.psi.MybatisAttrValue
import org.javamaster.mybatis.tools.psi.MybatisFieldName

/**
 * @author yudong
 */
object MybatisPsiImplUtil {

    @JvmStatic
    fun getName(mybatisFieldName: MybatisFieldName): String {
        return mybatisFieldName.text
    }

    @JvmStatic
    fun getReferences(mybatisFieldName: MybatisFieldName): Array<PsiReference> {
        return ReferenceProvidersRegistry.getReferencesFromProviders(mybatisFieldName)
    }

    @JvmStatic
    fun getName(mybatisAttrName: MybatisAttrName): String {
        return mybatisAttrName.text
    }

    @JvmStatic
    fun getReferences(mybatisAttrName: MybatisAttrName): Array<PsiReference> {
        return ReferenceProvidersRegistry.getReferencesFromProviders(mybatisAttrName)
    }

    @JvmStatic
    fun getName(mybatisAttrValue: MybatisAttrValue): String {
        return mybatisAttrValue.text
    }

    @JvmStatic
    fun getReferences(mybatisAttrValue: MybatisAttrValue): Array<PsiReference> {
        return ReferenceProvidersRegistry.getReferencesFromProviders(mybatisAttrValue)
    }

}