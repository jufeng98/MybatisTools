package org.javamaster.mybatis.tools.parser

import com.intellij.lexer.FlexAdapter
import org.javamaster.mybatis.tools._MybatisLexer

/**
 * @author yudong
 */
open class MybatisAdapter : FlexAdapter(_MybatisLexer(null))
