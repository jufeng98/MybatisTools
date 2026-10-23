// apache2.txt
package com.intellij.lang.ognl.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static com.intellij.lang.ognl.OgnlTypes.*;
import com.intellij.lang.ognl.psi.*;

public class OgnlMapExpressionImpl extends OgnlExpressionImpl implements OgnlMapExpression {

  public OgnlMapExpressionImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public void accept(@NotNull OgnlVisitor visitor) {
    visitor.visitMapExpression(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof OgnlVisitor) accept((OgnlVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<OgnlMapEntryElement> getMapEntryElementList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, OgnlMapEntryElement.class);
  }

  @Override
  @Nullable
  public OgnlFqnTypeExpression getMapType() {
    return findChildByClass(OgnlFqnTypeExpression.class);
  }

}
