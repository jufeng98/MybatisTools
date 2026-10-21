package com.intellij.lang.ognl.psi.manipulators;

import com.intellij.lang.ognl.OgnlFileType;
import com.intellij.lang.ognl.psi.OgnlReferenceExpression;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author yudong
 */
public class OgnlReferenceExpressionManipulator extends AbstractElementManipulator<OgnlReferenceExpression> {

    @Override
    public @Nullable OgnlReferenceExpression handleContentChange(@NotNull OgnlReferenceExpression referenceExpression,
                                                                 @NotNull TextRange textRange,
                                                                 @NotNull String newContent) throws IncorrectOperationException {
        String str = "%{" + textRange.replace(referenceExpression.getText(), newContent) + "}";

        PsiFile psiFile = PsiFileFactory.getInstance(referenceExpression.getProject())
                .createFileFromText("foo.ognl", OgnlFileType.INSTANCE, str);

        PsiElement element = PsiTreeUtil.findChildOfType(psiFile, OgnlReferenceExpression.class);

        //noinspection DataFlowIssue
        return (OgnlReferenceExpression) referenceExpression.replace(element);
    }

}
