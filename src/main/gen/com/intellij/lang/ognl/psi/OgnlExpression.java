// apache2.txt
package com.intellij.lang.ognl.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiType;

public interface OgnlExpression extends OgnlPsiCompositeElement {

  @Nullable PsiType getType();

}
