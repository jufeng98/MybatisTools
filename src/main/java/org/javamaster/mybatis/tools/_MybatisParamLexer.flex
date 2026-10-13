package org.javamaster.mybatis.tools;

import com.intellij.psi.tree.IElementType;

import static com.intellij.psi.TokenType.BAD_CHARACTER;
import static com.intellij.psi.TokenType.WHITE_SPACE;
import static org.javamaster.mybatis.tools.psi.MybatisParamTypes.*;

%%

%{
  public _MybatisLexer() {
    this((java.io.Reader)null);
  }
%}

%public
%class _MybatisLexer
%implements com.intellij.lexer.FlexLexer
%function advance
%type IElementType
%unicode
%state IN_ATTR_NAME, IN_ATTR_VALUE
%eof{
  return;
%eof}

EOL=\R
WHITE_SPACE=\s+

SEPARATOR=[,]
ATTR_NAME=[^\s,=]+
ATTR_SEPARATOR=[=]
ATTR_VALUE=[^\s,=]+

%%
<YYINITIAL> {
  {SEPARATOR}              { yybegin(IN_ATTR_NAME); return SEPARATOR; }
  {WHITE_SPACE}            { return WHITE_SPACE; }
}

<IN_ATTR_NAME> {
  {ATTR_NAME}               { return ATTR_NAME_LABEL; }
  {ATTR_SEPARATOR}          { yybegin(IN_ATTR_VALUE); return ATTR_SEPARATOR; }
  {WHITE_SPACE}             { return WHITE_SPACE; }
}

<IN_ATTR_VALUE> {
  {ATTR_VALUE}              { return ATTR_VALUE_LABEL; }
  {SEPARATOR}               { yybegin(IN_ATTR_NAME); return SEPARATOR; }
  {WHITE_SPACE}             { return WHITE_SPACE; }
}

[^] { return BAD_CHARACTER; }
