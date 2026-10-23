// apache2.txt
package com.intellij.lang.ognl.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface OgnlNewExpression extends OgnlExpression {

  @Nullable
  OgnlParameterList getParameterList();

  @Nullable
  OgnlFqnTypeExpression getObjectType();

}
