package org.javamaster.mybatis.tools;

import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.util.IconLoader;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

import static com.baomidou.plugin.idea.mybatisx.util.Icons.IMAGES_STATEMENT_SVG;

/**
 * @author yudong
 */
public class MybatisParamFileType extends LanguageFileType {

    public static final MybatisParamFileType INSTANCE = new MybatisParamFileType();
    public static final String DEFAULT_EXTENSION = "mybatisParam";

    private MybatisParamFileType() {
        super(MybatisParamLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public String getName() {
        return "MYBATIS_PARAM";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Mybatis xml file param";
    }

    @NotNull
    @Override
    public String getDefaultExtension() {
        return DEFAULT_EXTENSION;
    }

    @Override
    public Icon getIcon() {
        return IconLoader.getIcon(IMAGES_STATEMENT_SVG, this.getClass());
    }

}
