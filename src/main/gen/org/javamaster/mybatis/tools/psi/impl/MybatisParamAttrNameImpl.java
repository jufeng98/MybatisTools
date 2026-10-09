// This is a generated file. Not intended for manual editing.
package org.javamaster.mybatis.tools.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static org.javamaster.mybatis.tools.psi.MybatisParamTypes.*;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import org.javamaster.mybatis.tools.psi.*;
import com.intellij.psi.PsiReference;

public class MybatisParamAttrNameImpl extends ASTWrapperPsiElement implements MybatisParamAttrName {

  public MybatisParamAttrNameImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull MybatisParamVisitor visitor) {
    visitor.visitAttrName(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof MybatisParamVisitor) accept((MybatisParamVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  public @NotNull String getName() {
    return MybatisParamPsiImplUtil.getName(this);
  }

  @Override
  public @NotNull PsiReference @NotNull [] getReferences() {
    return MybatisParamPsiImplUtil.getReferences(this);
  }

}
