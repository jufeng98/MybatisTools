// apache2.txt
package com.intellij.lang.ognl.psi;

import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElementVisitor;

public class OgnlVisitor extends PsiElementVisitor {

  public void visitBinaryExpression(@NotNull OgnlBinaryExpression o) {
    visitExpression(o);
  }

  public void visitConditionalExpression(@NotNull OgnlConditionalExpression o) {
    visitExpression(o);
  }

  public void visitExpression(@NotNull OgnlExpression o) {
    visitPsiCompositeElement(o);
  }

  public void visitFqnTypeExpression(@NotNull OgnlFqnTypeExpression o) {
    visitExpression(o);
  }

  public void visitIndexedExpression(@NotNull OgnlIndexedExpression o) {
    visitExpression(o);
  }

  public void visitLambdaExpression(@NotNull OgnlLambdaExpression o) {
    visitExpression(o);
  }

  public void visitLiteralExpression(@NotNull OgnlLiteralExpression o) {
    visitExpression(o);
  }

  public void visitMapEntryElement(@NotNull OgnlMapEntryElement o) {
    visitPsiCompositeElement(o);
  }

  public void visitMapExpression(@NotNull OgnlMapExpression o) {
    visitExpression(o);
  }

  public void visitMethodCallExpression(@NotNull OgnlMethodCallExpression o) {
    visitExpression(o);
  }

  public void visitNewArrayExpression(@NotNull OgnlNewArrayExpression o) {
    visitExpression(o);
  }

  public void visitNewExpression(@NotNull OgnlNewExpression o) {
    visitExpression(o);
  }

  public void visitParameterList(@NotNull OgnlParameterList o) {
    visitPsiCompositeElement(o);
  }

  public void visitParenthesizedExpression(@NotNull OgnlParenthesizedExpression o) {
    visitExpression(o);
  }

  public void visitProjectionExpression(@NotNull OgnlProjectionExpression o) {
    visitExpression(o);
  }

  public void visitReferenceExpression(@NotNull OgnlReferenceExpression o) {
    visitExpression(o);
  }

  public void visitSelectionExpression(@NotNull OgnlSelectionExpression o) {
    visitExpression(o);
  }

  public void visitSequenceExpression(@NotNull OgnlSequenceExpression o) {
    visitExpression(o);
  }

  public void visitUnaryExpression(@NotNull OgnlUnaryExpression o) {
    visitExpression(o);
  }

  public void visitVariableAssignmentExpression(@NotNull OgnlVariableAssignmentExpression o) {
    visitExpression(o);
  }

  public void visitVariableExpression(@NotNull OgnlVariableExpression o) {
    visitExpression(o);
  }

  public void visitPsiCompositeElement(@NotNull OgnlPsiCompositeElement o) {
    visitElement(o);
  }

}
