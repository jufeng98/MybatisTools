// apache2.txt
package com.intellij.lang.ognl.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface OgnlMapExpression extends OgnlExpression {

  @NotNull
  List<OgnlMapEntryElement> getMapEntryElementList();

  @Nullable
  OgnlFqnTypeExpression getMapType();

}
