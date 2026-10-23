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

public class OgnlParameterListImpl extends OgnlPsiCompositeElementBase implements OgnlParameterList {

  public OgnlParameterListImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull OgnlVisitor visitor) {
    visitor.visitParameterList(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof OgnlVisitor) accept((OgnlVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<OgnlExpression> getParametersList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, OgnlExpression.class);
  }

  @Override
  public int getParameterCount() {
    return OgnlPsiUtil.getParameterCount(this);
  }

}
