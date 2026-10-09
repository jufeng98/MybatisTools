package com.baomidou.plugin.idea.mybatisx.tip.hashmark;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElement;
import org.jetbrains.annotations.NotNull;

/**
 * @author ls9527
 */
public class HashMarkTipInsertHandler implements InsertHandler<LookupElement> {

    @Override
    public void handleInsert(@NotNull InsertionContext context, @NotNull LookupElement item) {
        context.setLaterRunnable(() -> {
            CodeCompletionHandlerBase handler = CodeCompletionHandlerBase.createHandler(CompletionType.BASIC);
            handler.invokeCompletion(context.getProject(), context.getEditor(), 1, true);
        });
    }
}
