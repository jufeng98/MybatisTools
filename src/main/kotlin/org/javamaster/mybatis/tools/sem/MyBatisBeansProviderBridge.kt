package org.javamaster.mybatis.tools.sem

import com.intellij.psi.PsiPackage
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.spring.model.CommonSpringBean
import java.lang.reflect.Method

/**
 * @author yudong
 */
interface MyBatisBeansProviderBridge {

    fun processBasePackage(
        searchScope: GlobalSearchScope,
        mapperPackage: PsiPackage,
        mapperBeans: MutableSet<CommonSpringBean>,
    )

}

object ReflectiveBeansProviderBridge : MyBatisBeansProviderBridge {
    // 按版本优先顺序列出所有可能的类名
    private val candidateClasses = listOf(
        // 旧版本路径
        "com.intellij.spring.model.extensions.myBatis.SpringMyBatisBeansProvider",
        // 新版本路径 (2026.2.3+)
        "com.intellij.spring.core.analysis.jvm.providers.myBatis.SpringMyBatisBeansProvider",
    )

    // 缓存已解析的 Class 和 Method，避免重复反射开销
    private var cachedClass: Class<*>? = null
    private var cachedMethod: Method? = null
    private var resolutionAttempted = false

    /**
     * 调用 processBasePackage 的统一入口，对外暴露稳定的方法签名
     */
    override fun processBasePackage(
        searchScope: GlobalSearchScope,
        mapperPackage: PsiPackage,
        mapperBeans: MutableSet<CommonSpringBean>,
    ) {
        val method = resolveMethod() ?: return

        try {
            method.invoke(null, searchScope, mapperPackage, mapperBeans)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun resolveMethod(): Method? {
        // 已有缓存直接返回
        if (cachedMethod != null) return cachedMethod
        if (resolutionAttempted) return null
        resolutionAttempted = true

        for (className in candidateClasses) {
            try {
                val clazz = Class.forName(className)

                val method = clazz.getMethod(
                    "processBasePackage", GlobalSearchScope::class.java, PsiPackage::class.java, Collection::class.java
                )

                cachedClass = clazz
                cachedMethod = method
                return method
            } catch (_: ClassNotFoundException) {
                // 当前版本不存在该类，尝试下一个
            } catch (_: NoSuchMethodException) {
                // 类存在但方法签名不符，继续尝试
            }
        }

        return null
    }
}
