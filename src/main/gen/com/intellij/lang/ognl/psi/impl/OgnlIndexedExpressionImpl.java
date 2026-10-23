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
import com.intellij.psi.PsiReference;

public class OgnlIndexedExpressionImpl extends OgnlExpressionImpl implements OgnlIndexedExpression {

  public OgnlIndexedExpressionImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public void accept(@NotNull OgnlVisitor visitor) {
    visitor.visitIndexedExpression(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof OgnlVisitor) accept((OgnlVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public OgnlExpression getReferenceQualifier() {
    List<OgnlExpression> p1 = PsiTreeUtil.getChildrenOfTypeAsList(this, OgnlExpression.class);
    return p1.get(0);
  }

  @Override
  @Nullable
  public OgnlExpression getIndexExpression() {
    List<OgnlExpression> p1 = PsiTreeUtil.getChildrenOfTypeAsList(this, OgnlExpression.class);
    return p1.size() < 2 ? null : p1.get(1);
  }

  @Override
  public @NotNull PsiReference[] getReferences() {
    return OgnlPsiUtil.getReferences(this);
  }

  @Override
  public @Nullable PsiReference getReference() {
    return OgnlPsiUtil.getReference(this);
  }

}
