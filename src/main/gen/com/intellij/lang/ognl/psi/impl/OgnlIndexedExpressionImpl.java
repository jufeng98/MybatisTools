/*
 * Copyright 2018 The authors
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


// Generated from ognl.bnf, do not modify
package com.intellij.lang.ognl.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.lang.ognl.psi.*;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiTreeUtil;
import org.javamaster.mybatis.tools.utils.ReferenceUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class OgnlIndexedExpressionImpl extends OgnlExpressionImpl implements OgnlIndexedExpression {

    public OgnlIndexedExpressionImpl(ASTNode node) {
        super(node);
    }

    public void accept(@NotNull OgnlVisitor visitor) {
        visitor.visitIndexedExpression(this);
    }

    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof OgnlVisitor) accept((OgnlVisitor) visitor);
        else super.accept(visitor);
    }

    @Override
    @NotNull
    public OgnlExpression getReferenceQualifier() {
        List<OgnlExpression> p1 = PsiTreeUtil.getChildrenOfTypeAsList(this, OgnlExpression.class);
        return p1.get(0);
    }

    @Override
    public PsiReference @NotNull [] getReferences() {
        String text = this.getText();
        int idx = text.indexOf("].");
        if (idx == -1) {
            return new PsiReference[0];
        }

        int realIdx = idx + 2;
        String fieldName = text.substring(realIdx);

        List<TextRange> textRanges = ReferenceUtils.INSTANCE.convertFieldName(fieldName);

        textRanges = textRanges.stream()
                .map(it -> it.shiftRight(realIdx))
                .toList();

        return ReferenceUtils.INSTANCE.createIndexReferences(this, textRanges, text);
    }

    @Override
    public PsiReference getReference() {
        PsiReference[] references = getReferences();
        if (references.length == 1) {
            return references[0];
        } else {
            return null;
        }
    }

    @Override
    @Nullable
    public OgnlExpression getIndexExpression() {
        List<OgnlExpression> p1 = PsiTreeUtil.getChildrenOfTypeAsList(this, OgnlExpression.class);
        return p1.size() < 2 ? null : p1.get(1);
    }

}
