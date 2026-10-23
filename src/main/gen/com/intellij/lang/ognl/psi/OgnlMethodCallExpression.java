// apache2.txt
package com.intellij.lang.ognl.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface OgnlMethodCallExpression extends OgnlExpression {

  @Nullable
  OgnlParameterList getParameterList();

  @Nullable
  OgnlExpression getMethod();

}
