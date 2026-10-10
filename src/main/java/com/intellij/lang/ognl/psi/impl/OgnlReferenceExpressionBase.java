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

package com.intellij.lang.ognl.psi.impl;

import com.intellij.icons.AllIcons;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.lang.ASTNode;
import com.intellij.lang.ognl.OgnlTypes;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import org.javamaster.mybatis.tools.utils.ReferenceUtils;
import org.jetbrains.annotations.NotNull;

/**
 * @author Yann C&eacute;bron
 */
abstract class OgnlReferenceExpressionBase extends OgnlExpressionImpl {

    protected OgnlReferenceExpressionBase(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public ItemPresentation getPresentation() {
        return new PresentationData(getIdentifier().getText(), null, AllIcons.Nodes.Parameter, null);
    }

    private PsiElement getIdentifier() {
        return findNotNullChildByType(OgnlTypes.IDENTIFIER);
    }

    @Override
    public PsiReference @NotNull [] getReferences() {
        String text = this.getText();

        return ReferenceUtils.INSTANCE.convert(text, this);
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
}
