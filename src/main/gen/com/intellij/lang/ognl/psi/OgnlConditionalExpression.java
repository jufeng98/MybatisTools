// apache2.txt
package com.intellij.lang.ognl.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface OgnlConditionalExpression extends OgnlExpression {

  @NotNull
  OgnlExpression getCondition();

  @Nullable
  OgnlExpression getThen();

  @Nullable
  OgnlExpression getElse();

}
