package org.javamaster.mybatis.tools.psi

import com.intellij.psi.tree.IElementType
import org.javamaster.mybatis.tools.MybatisParamLanguage
import org.jetbrains.annotations.NonNls

class MybatisParamElementType(debugName: @NonNls String) : IElementType(debugName, MybatisParamLanguage.INSTANCE)
