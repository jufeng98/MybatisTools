// apache2.txt
package com.intellij.lang.ognl.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;

public interface OgnlIndexedExpression extends OgnlExpression {

  @NotNull
  OgnlExpression getReferenceQualifier();

  @Nullable
  OgnlExpression getIndexExpression();

  @NotNull PsiReference[] getReferences();

  @Nullable PsiReference getReference();

}
