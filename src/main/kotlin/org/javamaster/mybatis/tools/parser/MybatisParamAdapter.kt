package org.javamaster.mybatis.tools.parser

import com.intellij.lexer.FlexAdapter
import org.javamaster.mybatis.tools._MybatisParamLexer

/**
 * @author yudong
 */
open class MybatisParamAdapter : FlexAdapter(_MybatisParamLexer(null))
