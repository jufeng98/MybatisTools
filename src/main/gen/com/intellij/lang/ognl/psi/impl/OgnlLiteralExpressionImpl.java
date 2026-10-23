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

public class OgnlLiteralExpressionImpl extends OgnlExpressionImpl implements OgnlLiteralExpression {

  public OgnlLiteralExpressionImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public void accept(@NotNull OgnlVisitor visitor) {
    visitor.visitLiteralExpression(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof OgnlVisitor) accept((OgnlVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  public @Nullable Object getConstantValue() {
    return OgnlPsiUtil.getConstantValue(this);
  }

}
