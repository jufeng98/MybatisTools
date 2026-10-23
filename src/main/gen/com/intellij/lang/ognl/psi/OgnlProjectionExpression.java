// apache2.txt
package com.intellij.lang.ognl.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface OgnlProjectionExpression extends OgnlExpression {

  @NotNull
  OgnlExpression getProjectionExpression();

}
