package org.javamaster.mybatis.tools.utils

import com.intellij.openapi.util.TextRange

object ReferenceUtils {

    fun convert(text: String): MutableList<TextRange> {
        val textRangeList: MutableList<TextRange> = mutableListOf()

        var lastIdx = 0
        var idx = text.indexOf(".")
        while (idx != -1) {
            val textRange = TextRange(lastIdx, idx)
            textRangeList.add(textRange)

            lastIdx = idx + 1
            idx = text.indexOf(".", lastIdx)
        }

        val textRange = TextRange(lastIdx, text.length)
        textRangeList.add(textRange)

        return textRangeList
    }

}
