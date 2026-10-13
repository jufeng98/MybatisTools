package org.javamaster.mybatis.tools.contributor

import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.*
import com.intellij.util.ProcessingContext
import org.javamaster.mybatis.tools.psi.MybatisParamAttrName
import org.javamaster.mybatis.tools.psi.MybatisParamAttrValue
import org.javamaster.mybatis.tools.reference.MybatisAttrNameReference
import org.javamaster.mybatis.tools.reference.MybatisAttrValueReference
import org.javamaster.mybatis.tools.utils.InjectionUtils

/***
 * mapper.xml文件 #{} / ${} 区域的 param 属性及属性值智能提示及引用跳转
 *
 * @author yudong
 */
class MybatisReferenceContributor : PsiReferenceContributor() {

    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(MybatisParamAttrName::class.java),
            object : PsiReferenceProvider() {

                override fun getReferencesByElement(
                    psiElement: PsiElement,
                    processingContext: ProcessingContext,
                ): Array<PsiReference> {
                    return arrayOf(MybatisAttrNameReference(psiElement, InjectionUtils.innerRange(psiElement)))
                }

            })

        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(MybatisParamAttrValue::class.java),
            object : PsiReferenceProvider() {

                override fun getReferencesByElement(
                    psiElement: PsiElement,
                    processingContext: ProcessingContext,
                ): Array<PsiReference> {
                    return arrayOf(MybatisAttrValueReference(psiElement, InjectionUtils.innerRange(psiElement)))
                }

            })
    }

}