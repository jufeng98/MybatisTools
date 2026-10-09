package org.javamaster.mybatis.tools.psi

import com.intellij.psi.tree.IElementType
import org.javamaster.mybatis.tools.MybatisLanguage
import org.jetbrains.annotations.NonNls

/**
 * @author yudong
 */
class MybatisTokenType(debugName: @NonNls String) : IElementType(debugName, MybatisLanguage.INSTANCE) {

    override fun toString(): String {
        return MybatisTokenType::class.java.simpleName + "." + super.toString()
    }

}
