package org.javamaster.mybatis.tools.psi.impl

import com.intellij.psi.PsiReference
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry
import org.javamaster.mybatis.tools.psi.MybatisParamAttrName
import org.javamaster.mybatis.tools.psi.MybatisParamAttrValue

/**
 * @author yudong
 */
object MybatisParamPsiImplUtil {

    @JvmStatic
    fun getName(mybatisAttrName: MybatisParamAttrName): String {
        return mybatisAttrName.text
    }

    @JvmStatic
    fun getReferences(mybatisAttrName: MybatisParamAttrName): Array<PsiReference> {
        return ReferenceProvidersRegistry.getReferencesFromProviders(mybatisAttrName)
    }

    @JvmStatic
    fun getName(mybatisAttrValue: MybatisParamAttrValue): String {
        return mybatisAttrValue.text
    }

    @JvmStatic
    fun getReferences(mybatisAttrValue: MybatisParamAttrValue): Array<PsiReference> {
        return ReferenceProvidersRegistry.getReferencesFromProviders(mybatisAttrValue)
    }

}