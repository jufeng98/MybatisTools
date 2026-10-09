package org.javamaster.mybatis.tools.psi

import com.intellij.psi.tree.IElementType
import org.javamaster.mybatis.tools.MybatisLanguage
import org.jetbrains.annotations.NonNls

class MybatisElementType(debugName: @NonNls String) : IElementType(debugName, MybatisLanguage.INSTANCE)
