package org.javamaster.mybatis.tools.contributor

import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.*
import com.intellij.util.ProcessingContext
import org.javamaster.mybatis.tools.psi.MybatisParamAttrName
import org.javamaster.mybatis.tools.psi.MybatisParamFieldName
import org.javamaster.mybatis.tools.reference.MybatisAttrNameReference
import org.javamaster.mybatis.tools.reference.MybatisFieldNameReference
import org.javamaster.mybatis.tools.utils.ReferenceUtils

/***
 * mapper.xml文件 #{} / ${} 区域的引用跳转
 *
 * @author yudong
 */
class MybatisReferenceContributor : PsiReferenceContributor() {

    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(MybatisParamFieldName::class.java),
            object : PsiReferenceProvider() {

                override fun getReferencesByElement(
                    psiElement: PsiElement,
                    processingContext: ProcessingContext,
                ): Array<MybatisFieldNameReference> {
                    val text = psiElement.text

                    return ReferenceUtils.convert(text, psiElement)
                }

            })

        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(MybatisParamAttrName::class.java),
            object : PsiReferenceProvider() {

                override fun getReferencesByElement(
                    psiElement: PsiElement,
                    processingContext: ProcessingContext,
                ): Array<PsiReference> {
                    return arrayOf(MybatisAttrNameReference(psiElement, psiElement.textRangeInParent))
                }

            })
    }

}