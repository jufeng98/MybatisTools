package com.baomidou.plugin.idea.mybatisx.reference;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.util.ReferenceSetBase;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.sql.psi.SqlExpression;
import org.javamaster.mybatis.tools.utils.XmlSqlUtils;
import org.jetbrains.annotations.*;

import java.util.Objects;
import java.util.Set;

/**
 * The type Result property reference set.
 *
 * @author yanglin
 */
public class ResultColumnReferenceSet extends ReferenceSetBase<PsiReference> {

    private final PsiClass mapperClass;

    /**
     * Instantiates a new Result property reference set.
     *
     * @param text        the text
     * @param element     the element
     * @param offset      the offset
     */
    public ResultColumnReferenceSet(String text, @NotNull PsiElement element, int offset, PsiClass mapperClass) {
        super(text, element, offset, DOT_SEPARATOR);
        this.mapperClass = mapperClass;
    }

    @Nullable
    @NonNls
    @Override
    protected PsiReference createReference(TextRange range, int index) {
        XmlAttributeValue element = (XmlAttributeValue) getElement();
        if (null == element) {
            return null;
        }

        PsiElement xmlAttr = element.getParent();
        if (xmlAttr instanceof XmlAttribute xmlAttribute) {
            Set<SqlExpression> sqlExpressions = XmlSqlUtils.INSTANCE.getRelateColumnElements(xmlAttribute);
            if (sqlExpressions.isEmpty()) {
                return null;
            }

            boolean match = sqlExpressions.stream()
                    .anyMatch(it -> Objects.equals("*", it.getName()));
            if (match) {
                return null;
            }
        }

        return new ContextPsiColumnReference(element, range, index, mapperClass);
    }

}
