package org.javamaster.mybatis.tools.highlighting;

import com.google.common.collect.Maps;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.tree.IElementType;
import org.javamaster.mybatis.tools.parser.MybatisAdapter;
import org.javamaster.mybatis.tools.psi.MybatisParamTypes;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * @author yudong
 */
public class MybatisParamSyntaxHighlighter extends SyntaxHighlighterBase {
    private static final Map<IElementType, TextAttributesKey> ATTRIBUTE_MAP = Maps.newHashMap();

    private static final TextAttributesKey ATTR_NAME;
    private static final TextAttributesKey ATTR_VALUE;

    static {
        ATTR_NAME = TextAttributesKey.createTextAttributesKey("MYBATIS_TOOLS_ATTR_NAME", DefaultLanguageHighlighterColors.PARAMETER);
        ATTR_VALUE = TextAttributesKey.createTextAttributesKey("MYBATIS_TOOLS_ATTR_VALUE", DefaultLanguageHighlighterColors.STRING);

        fillMap(ATTRIBUTE_MAP, ATTR_NAME, MybatisParamTypes.ATTR_NAME_LABEL);
        fillMap(ATTRIBUTE_MAP, ATTR_VALUE, MybatisParamTypes.ATTR_VALUE_LABEL);
    }

    public @NotNull Lexer getHighlightingLexer() {
        return new MybatisAdapter();
    }

    public TextAttributesKey @NotNull [] getTokenHighlights(IElementType tokenType) {
        return pack(ATTRIBUTE_MAP.get(tokenType));
    }
}
