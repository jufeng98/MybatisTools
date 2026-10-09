package org.javamaster.mybatis.tools.parser

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider
import org.javamaster.mybatis.tools.MybatisParamFileType
import org.javamaster.mybatis.tools.MybatisParamLanguage

/**
 * @author yudong
 */
class MybatisFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, MybatisParamLanguage.INSTANCE) {

    override fun getFileType(): FileType {
        return MybatisParamFileType.INSTANCE
    }

    override fun toString(): String {
        return "Mybatis File"
    }
}
