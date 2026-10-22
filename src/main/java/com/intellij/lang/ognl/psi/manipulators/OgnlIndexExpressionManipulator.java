package com.intellij.lang.ognl.psi.manipulators;

import com.intellij.lang.ognl.OgnlFileType;
import com.intellij.lang.ognl.psi.OgnlIndexedExpression;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author yudong
 */
public class OgnlIndexExpressionManipulator extends AbstractElementManipulator<OgnlIndexedExpression> {

    @Override
    public @Nullable OgnlIndexedExpression handleContentChange(@NotNull OgnlIndexedExpression indexExpression,
                                                               @NotNull TextRange textRange,
                                                               @NotNull String newContent) throws IncorrectOperationException {
        String str = "%{" + textRange.replace(indexExpression.getText(), newContent) + "}";

        PsiFile psiFile = PsiFileFactory.getInstance(indexExpression.getProject())
                .createFileFromText("foo.ognl", OgnlFileType.INSTANCE, str);

        PsiElement element = PsiTreeUtil.findChildOfType(psiFile, OgnlIndexedExpression.class);

        //noinspection DataFlowIssue
        return (OgnlIndexedExpression) indexExpression.replace(element);
    }

}
