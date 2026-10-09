package org.javamaster.mybatis.tools.contributor

import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.*
import com.intellij.util.ProcessingContext
import org.javamaster.mybatis.tools.psi.MybatisAttrName
import org.javamaster.mybatis.tools.psi.MybatisFieldName
import org.javamaster.mybatis.tools.reference.MybatisAttrNameReference
import org.javamaster.mybatis.tools.reference.MybatisFieldNameReference
import org.javamaster.mybatis.tools.utils.ReferenceUtils
import java.util.*

/***
 * mapper.xml文件 #{} / ${} 区域的引用跳转
 *
 * @author yudong
 */
class MybatisReferenceContributor : PsiReferenceContributor() {

    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(MybatisFieldName::class.java),
            object : PsiReferenceProvider() {

                override fun getReferencesByElement(
                    psiElement: PsiElement,
                    processingContext: ProcessingContext,
                ): Array<PsiReference> {
                    val text = psiElement.text

                    val textRangeList = ReferenceUtils.convert(text)

                    val psiReferences = mutableListOf<PsiReference>()

                    for (i in textRangeList.indices) {
                        val textRange = textRangeList[i]

                        val nameLevels = LinkedList<String>()
                        for (j in 0..<i + 1) {
                            nameLevels.add(textRangeList[j].substring(text))
                        }

                        psiReferences.add(MybatisFieldNameReference(psiElement, textRange, nameLevels))
                    }

                    return psiReferences.toTypedArray()
                }

            })

        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(MybatisAttrName::class.java),
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