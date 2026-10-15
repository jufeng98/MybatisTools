package org.javamaster.mybatis.tools.contributor

import com.intellij.openapi.project.Project
import com.intellij.patterns.PsiJavaPatterns
import com.intellij.semantic.SemContributor
import com.intellij.semantic.SemRegistrar
import org.javamaster.mybatis.tools.sem.SpringMyBatisMapperScanJamComponent
import org.javamaster.mybatis.tools.sem.SpringTkMyBatisMapperScanJamComponent
import org.javamaster.mybatis.tools.sem.SpringTkMyBatisMapperScanJamComponent.Companion.TK_MAPPER_SCAN_ANNO

/**
 * 解决在 Spring Service 中注入 Mybatis Mapper 时的红色波浪线错误问题
 *
 * @author yudong
 */
class MybatisSemContributor : SemContributor() {

    override fun registerSemProviders(registrar: SemRegistrar, project: Project) {
        val pattern = PsiJavaPatterns.psiClass()
            .withAnnotation(com.intellij.spring.model.extensions.myBatis.SpringMyBatisMapperScanJamComponent.MAPPER_SCAN_ANNOTATION)

        SpringMyBatisMapperScanJamComponent.jamClassMeta.register(registrar, pattern)

        val tkPattern = PsiJavaPatterns.psiClass().withAnnotation(TK_MAPPER_SCAN_ANNO)

        SpringTkMyBatisMapperScanJamComponent.jamClassMeta.register(registrar, tkPattern)
    }

}