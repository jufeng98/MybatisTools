package org.javamaster.mybatis.tools.handler

import com.intellij.codeInsight.completion.BasicInsertHandler
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElement

object EqualsEndInsertHandler : BasicInsertHandler<LookupElement>() {

    override fun handleInsert(context: InsertionContext, item: LookupElement) {
        val editor = context.editor
        val document = editor.document

        context.commitDocument()

        val offset = context.tailOffset

        document.insertString(offset, "=")

        editor.caretModel.moveToOffset(offset + 1)
    }

}
