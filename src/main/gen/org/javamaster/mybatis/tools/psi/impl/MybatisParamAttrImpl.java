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

public class MybatisParamAttrImpl extends ASTWrapperPsiElement implements MybatisParamAttr {

  public MybatisParamAttrImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull MybatisParamVisitor visitor) {
    visitor.visitAttr(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof MybatisParamVisitor) accept((MybatisParamVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public MybatisParamAttrName getAttrName() {
    return findNotNullChildByClass(MybatisParamAttrName.class);
  }

  @Override
  @Nullable
  public MybatisParamAttrValue getAttrValue() {
    return findChildByClass(MybatisParamAttrValue.class);
  }

}
