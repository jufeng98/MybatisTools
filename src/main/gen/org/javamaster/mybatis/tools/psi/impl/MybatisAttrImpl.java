// This is a generated file. Not intended for manual editing.
package org.javamaster.mybatis.tools.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static org.javamaster.mybatis.tools.psi.MybatisTypes.*;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import org.javamaster.mybatis.tools.psi.*;

public class MybatisAttrImpl extends ASTWrapperPsiElement implements MybatisAttr {

  public MybatisAttrImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull MybatisVisitor visitor) {
    visitor.visitAttr(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof MybatisVisitor) accept((MybatisVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public MybatisAttrName getAttrName() {
    return findNotNullChildByClass(MybatisAttrName.class);
  }

  @Override
  @Nullable
  public MybatisAttrValue getAttrValue() {
    return findChildByClass(MybatisAttrValue.class);
  }

}
