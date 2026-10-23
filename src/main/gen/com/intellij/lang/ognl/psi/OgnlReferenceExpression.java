// apache2.txt
package com.intellij.lang.ognl.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface OgnlReferenceExpression extends OgnlExpression {

  @NotNull
  List<OgnlExpression> getExpressionList();

}
