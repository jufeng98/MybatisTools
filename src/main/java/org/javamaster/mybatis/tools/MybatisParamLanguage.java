package org.javamaster.mybatis.tools;

import com.intellij.lang.Language;

import static org.javamaster.mybatis.tools.MybatisParamFileType.DEFAULT_EXTENSION;

/**
 * @author yudong
 */
public class MybatisParamLanguage extends Language {

    public static final MybatisParamLanguage INSTANCE = new MybatisParamLanguage();

    private MybatisParamLanguage() {
        super(DEFAULT_EXTENSION);
    }

}
