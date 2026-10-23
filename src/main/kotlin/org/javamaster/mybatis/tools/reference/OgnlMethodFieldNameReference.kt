package org.javamaster.mybatis.tools.reference

import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.util.PropertyUtil
import com.intellij.psi.util.PsiTreeUtil
import org.apache.commons.collections.CollectionUtils
import org.javamaster.mybatis.tools.utils.PsiUtils
import org.javamaster.mybatis.tools.utils.ReferenceUtils
import java.util.*

/**
 * @author yudong
 */
class OgnlMethodFieldNameReference(
    psiElement: PsiElement,
    val from: TextRange,
    val parent: Array<PsiReference>,
    private val nameLevels: LinkedList<String>,
) : PsiReferenceBase<PsiElement>(psiElement, from) {

    override fun isReferenceTo(element: PsiElement): Boolean {
        val resolve = resolve()

        val equals = resolve == element
        if (equals) {
            return true
        }

        if (resolve is PsiField && element is PsiMethod) {
            if (!PropertyUtil.isSimpleGetter(element)) return false

            val propertyName = PropertyUtil.getPropertyName(element)
            if (resolve.name != propertyName) {
                return false
            }

            val resolvedClass = PsiTreeUtil.getParentOfType(resolve, PsiClass::class.java)
            val targetClass = PsiTreeUtil.getParentOfType(element, PsiClass::class.java)

            return resolvedClass == targetClass
        }

        return false
    }

    override fun resolve(): PsiElement? {
        val lastPsiReference = parent.last() as? OgnlFieldNameReference ?: return null
        val parentResolve = lastPsiReference.resolve() ?: return null

        if (parentResolve is PsiMethod) {
            val returnType = parentResolve.returnType
            val psiClass = PsiUtils.resolvePsiType(returnType) ?: return null

            return ReferenceUtils.resolveTarget(myElement, psiClass, nameLevels, emptyMap()) ?: psiClass
        }

        return null
    }

    override fun equals(other: Any?): Boolean {
        if (other !is OgnlMethodFieldNameReference) {
            return false
        }

        if (myElement != other.myElement) {
            return false
        }

        if (!CollectionUtils.isEqualCollection(nameLevels, other.nameLevels)) {
            return false
        }

        return from.substring(myElement.text) == other.from.substring(other.myElement.text)
    }

    override fun hashCode(): Int {
        var result = myElement.hashCode()
        result = 31 * result + from.hashCode()
        result = 31 * result + nameLevels.hashCode()
        return result
    }
}