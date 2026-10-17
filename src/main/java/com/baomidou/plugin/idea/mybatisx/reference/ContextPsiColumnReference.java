package com.baomidou.plugin.idea.mybatisx.reference;

import com.baomidou.plugin.idea.mybatisx.smartjpa.component.mapping.EntityMappingHolder;
import com.baomidou.plugin.idea.mybatisx.smartjpa.component.mapping.EntityMappingResolverFactory;
import com.google.common.collect.Lists;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.database.model.*;
import com.intellij.database.psi.*;
import com.intellij.database.util.DasUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.sql.psi.SqlExpression;
import com.intellij.util.containers.JBIterable;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.javamaster.mybatis.tools.utils.XmlSqlUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * The type Context psi field reference.
 *
 * @author yanglin
 */
public class ContextPsiColumnReference extends PsiReferenceBase<XmlAttributeValue> {

    private final PsiClass mapperClass;
    /**
     * The Resolver.
     * -- GETTER --
     * Gets resolver.
     * <p>
     * <p>
     * -- SETTER --
     * Sets resolver.
     */
    @Setter
    @Getter
    protected PsiColumnReferenceSetResolver resolver;
    /**
     * The Index.
     * -- GETTER --
     * Gets index.
     * <p>
     * <p>
     * -- SETTER --
     * Sets index.
     */
    @Setter
    @Getter
    protected int index;

    /**
     * Instantiates a new Context psi field reference.
     *
     * @param element     the element
     * @param range       the range
     * @param index       the index
     */
    public ContextPsiColumnReference(XmlAttributeValue element, TextRange range, int index, PsiClass mapperClass) {
        super(element, range, false);
        this.index = index;
        resolver = new PsiColumnReferenceSetResolver(element);
        this.mapperClass = mapperClass;
    }

    /**
     * 如果能找到正确的列, 线条转到正确的列
     * 无法找到数据库的列, 引用当前节点
     */
    @Nullable
    @Override
    public PsiElement resolve() {
        Optional<DasTable> resolved = resolver.resolve(index);
        // 找到表则加入字段验证
        if (resolved.isPresent()) {
            DasTable dasTable = resolved.get();

            Optional<DbElement> columns = resolver.findColumns(dasTable);
            if (columns.isPresent()) {
                return columns.get();
            }
        }

        XmlAttributeValue xmlAttrValue = resolver.getElement();
        PsiElement xmlAttr = xmlAttrValue.getParent();
        if (xmlAttr instanceof XmlAttribute xmlAttribute) {
            // 从 SQL 中找
            String columnName = xmlAttrValue.getValue();

            Set<SqlExpression> sqlExpressions = XmlSqlUtils.INSTANCE.getRelateColumnElements(xmlAttribute);
            if (sqlExpressions == null) {
                return null;
            }

            Optional<SqlExpression> first = sqlExpressions.stream()
                    .filter(it -> Objects.equals(it.getName(), columnName))
                    .findFirst();

            return first.orElse(null);
        }

        return null;
    }

    /**
     * 获取用于提示的变量列表
     */
    @Override
    public Object @NotNull [] getVariants() {
        Project project = getElement().getProject();
        List<Object> elementList = Lists.newArrayList();

        EntityMappingResolverFactory entityMappingResolverFactory = new EntityMappingResolverFactory(project);

        EntityMappingHolder entityMappingHolder = entityMappingResolverFactory.searchEntity(mapperClass);

        String tableName = entityMappingHolder.getTableName();
        if (StringUtils.isNotBlank(tableName)) {
            DbPsiFacade dbPsiFacade = DbPsiFacade.getInstance(project);

            List<Object> dbElements = getDbElements(tableName, dbPsiFacade);

            elementList.addAll(dbElements);
        }

        XmlAttributeValue xmlAttrValue = resolver.getElement();
        PsiElement xmlAttr = xmlAttrValue.getParent();
        if (xmlAttr instanceof XmlAttribute xmlAttribute) {
            Set<SqlExpression> columnNames = XmlSqlUtils.INSTANCE.getRelateColumnElements(xmlAttribute);
            if (columnNames != null) {
                columnNames.forEach(it -> {
                    LookupElementBuilder elementBuilder = LookupElementBuilder.create(it, StringUtils.defaultString(it.getName()));

                    elementList.add(elementBuilder);
                });
            }
        }

        return elementList.toArray();
    }

    private List<Object> getDbElements(String tableName, DbPsiFacade dbPsiFacade) {
        List<Object> resultList = Lists.newArrayList();
        for (DbDataSource dataSource : dbPsiFacade.getDataSources()) {
            JBIterable<? extends DasNamespace> schemas = DasUtil.getSchemas(dataSource);

            for (DasNamespace schema : schemas) {
                DasTable dasTable = DasUtil.findChild(schema, DasTable.class, ObjectKind.TABLE, tableName);
                if (dasTable == null) {
                    continue;
                }

                JBIterable<? extends DasColumn> columns = DasUtil.getColumns(dasTable);
                for (DasColumn column : columns) {
                    if (column instanceof DbElement dbElement) {
                        resultList.add(dbElement);
                    }
                }
            }
        }

        return resultList;
    }

}
