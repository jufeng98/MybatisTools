package org.javamaster.mybatis.tools;

import com.intellij.lang.Language;

import static org.javamaster.mybatis.tools.MybatisFileType.DEFAULT_EXTENSION;

/**
 * @author yudong
 */
public class MybatisLanguage extends Language {

    public static final MybatisLanguage INSTANCE = new MybatisLanguage();

    private MybatisLanguage() {
        super(DEFAULT_EXTENSION);
    }

}
