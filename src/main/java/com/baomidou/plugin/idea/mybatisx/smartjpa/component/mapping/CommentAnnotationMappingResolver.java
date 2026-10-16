package com.baomidou.plugin.idea.mybatisx.smartjpa.component.mapping;


import com.baomidou.plugin.idea.mybatisx.smartjpa.component.TxField;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.javadoc.*;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Optional;

/**
 * 在接口上加入注释, 注释内部加入注解:  @Entity com.xx.xx.User
 */
public class CommentAnnotationMappingResolver extends JpaMappingResolver implements EntityMappingResolver {
    /**
     * The constant TABLE_ENTITY.
     */
    public static final String TABLE_ENTITY = "Entity";

    public static final String TABLE_NAME = "TableName";

    @Override
    public List<TxField> findFields(PsiClass mapperClass, PsiClass entityClass) {
        return initDataByCamel(entityClass);
    }

    @Override
    public Optional<PsiClass> findEntity(PsiClass mapperClass) {
        PsiDocComment docComment = mapperClass.getDocComment();
        if (docComment == null) {
            return Optional.empty();
        }

        PsiDocTag tableEntity = docComment.findTagByName(TABLE_ENTITY);
        if (tableEntity == null) {
            return Optional.empty();
        }

        PsiDocTagValue valueElement = tableEntity.getValueElement();
        if (valueElement == null) {
            return Optional.empty();
        }

        String entityText = valueElement.getText();
        if (StringUtils.isBlank(entityText)) {
            return Optional.empty();
        }

        JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(mapperClass.getProject());
        PsiClass entityClass = javaPsiFacade.findClass(entityText, mapperClass.getResolveScope());
        if (entityClass == null) {
            return Optional.empty();
        }

        return Optional.of(entityClass);

    }

    @Override
    public Optional<String> findTableName(PsiClass entityClass) {
        PsiDocComment docComment = entityClass.getDocComment();
        if (docComment == null) {
            return getTableNameByJpa(entityClass);
        }

        PsiDocTag tableNameTag = docComment.findTagByName(TABLE_NAME);
        if (tableNameTag == null) {
            return getTableNameByJpa(entityClass);
        }

        PsiDocTagValue valueElement = tableNameTag.getValueElement();
        if (valueElement == null) {
            return getTableNameByJpa(entityClass);
        }

        String tableNameText = valueElement.getText();
        if (StringUtils.isBlank(tableNameText)) {
            return getTableNameByJpa(entityClass);
        }

        return Optional.of(tableNameText);
    }


}
