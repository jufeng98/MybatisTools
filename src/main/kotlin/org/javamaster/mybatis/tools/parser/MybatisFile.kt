package org.javamaster.mybatis.tools.parser

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider
import org.javamaster.mybatis.tools.MybatisFileType
import org.javamaster.mybatis.tools.MybatisLanguage

/**
 * @author yudong
 */
class MybatisFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, MybatisLanguage.INSTANCE) {

    override fun getFileType(): FileType {
        return MybatisFileType.INSTANCE
    }

    override fun toString(): String {
        return "Mybatis File"
    }
}
