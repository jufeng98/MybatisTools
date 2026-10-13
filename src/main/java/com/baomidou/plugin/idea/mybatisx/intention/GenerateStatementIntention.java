package com.baomidou.plugin.idea.mybatisx.intention;

import com.baomidou.plugin.idea.mybatisx.setting.config.AbstractStatementGenerator;
import com.intellij.codeInsight.intention.preview.IntentionPreviewUtils;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

/**
 * The type Generate statement intention.
 *
 * @author yanglin
 */
public class GenerateStatementIntention extends GenericIntention {

    /**
     * Instantiates a new Generate statement intention.
     */
    public GenerateStatementIntention() {
        super(GenerateStatementChooser.INSTANCE);
    }

    @NotNull
    @Override
    public String getText() {
        return "[MybatisTools] Generate new statement";
    }


    @Override
    public void invoke(@NotNull final Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
        if (IntentionPreviewUtils.isIntentionPreviewActive()) {
            return;
        }

        PsiElement element = file.findElementAt(editor.getCaretModel().getOffset());
        AbstractStatementGenerator.applyGenerate(PsiTreeUtil.getParentOfType(element, PsiMethod.class), project);
    }

}
