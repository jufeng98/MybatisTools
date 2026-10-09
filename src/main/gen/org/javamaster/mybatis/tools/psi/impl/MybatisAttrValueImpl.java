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
import com.intellij.psi.PsiReference;

public class MybatisAttrValueImpl extends ASTWrapperPsiElement implements MybatisAttrValue {

  public MybatisAttrValueImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull MybatisVisitor visitor) {
    visitor.visitAttrValue(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof MybatisVisitor) accept((MybatisVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  public @NotNull String getName() {
    return MybatisPsiImplUtil.getName(this);
  }

  @Override
  public @NotNull PsiReference @NotNull [] getReferences() {
    return MybatisPsiImplUtil.getReferences(this);
  }

}
