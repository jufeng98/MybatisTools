package org.javamaster.mybatis.tools.psi

import com.intellij.psi.tree.IElementType
import org.javamaster.mybatis.tools.MybatisParamLanguage
import org.jetbrains.annotations.NonNls

/**
 * @author yudong
 */
class MybatisParamTokenType(debugName: @NonNls String) : IElementType(debugName, MybatisParamLanguage.INSTANCE) {

    override fun toString(): String {
        return MybatisParamTokenType::class.java.simpleName + "." + super.toString()
    }

}
