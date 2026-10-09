package org.javamaster.mybatis.tools.parser;

import com.intellij.lang.*;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import org.javamaster.mybatis.tools.MybatisParamLanguage;
import org.javamaster.mybatis.tools.psi.MybatisParamTypes;
import org.jetbrains.annotations.NotNull;

/**
 * @author yudong
 */
public class MybatisParserDefinition implements ParserDefinition {
    public static final IFileElementType FILE = new IFileElementType(MybatisParamLanguage.INSTANCE);

    @Override
    public @NotNull Lexer createLexer(Project project) {
        return new MybatisAdapter();
    }

    @Override
    public @NotNull PsiParser createParser(Project project) {
        return new MybatisParamParser();
    }

    @Override
    public @NotNull IFileElementType getFileNodeType() {
        return FILE;
    }

    @Override
    public @NotNull TokenSet getCommentTokens() {
        return TokenSet.EMPTY;
    }

    @Override
    public @NotNull TokenSet getStringLiteralElements() {
        return TokenSet.EMPTY;
    }

    @Override
    public @NotNull PsiElement createElement(ASTNode node) {
        return MybatisParamTypes.Factory.createElement(node);
    }

    @Override
    public @NotNull PsiFile createFile(@NotNull FileViewProvider viewProvider) {
        return new MybatisFile(viewProvider);
    }
}
