package com.baomidou.plugin.idea.mybatisx.tip.hashmark;

import com.baomidou.plugin.idea.mybatisx.annotation.Annotation;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.PsiClassReferenceType;
import com.intellij.psi.util.InheritanceUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlTag;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.javamaster.mybatis.tools.reference.MybatisFieldNameReference;
import org.javamaster.mybatis.tools.utils.PsiUtils;
import org.javamaster.mybatis.tools.utils.ReferenceUtils;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * #{}提示的内容, 不仅仅只是字段， 还包括mybatis支持的7大属性配置
 *
 * @author ls9527
 */
@Slf4j
public class CompositeHashMarkTip {
    public static final List<HashMarkTip> hashMarkTips = new ArrayList<>() {
        {
            add(new JdbcTypeHashMarkTip());
            add(new TypeHandlerHashMarkTip());
            add(new JavaTypeHashMark());
            add(new ResultMapHashMarkTip());
            add(new ModeHashMarkTip());
            add(new NumericScaleHashMarkTip());
            add(new JdbcTypeNameHashMarkTip());
        }
    };

    private CompositeHashMarkTip() {
    }

    public static @Nullable XmlAttributeValue getCollectionAttrIfForeachTagItemAttrValue(PsiElement injectionHost) {
        if (!(injectionHost instanceof XmlAttributeValue xmlAttributeValue)) {
            return null;
        }

        XmlAttribute xmlAttribute = (XmlAttribute) xmlAttributeValue.getParent();
        if (!"item".equals(xmlAttribute.getName())) {
            return null;
        }

        XmlTag xmlTag = xmlAttribute.getParent();
        XmlAttribute collectionAttr = xmlTag.getAttribute("collection");
        if (collectionAttr == null) {
            return null;
        }

        return collectionAttr.getValueElement();
    }

    public static @Nullable PsiClass resolveGenericIfForeachTagItemAttrValue(XmlAttributeValue collectionAttrValue) {
        String collectionValue = collectionAttrValue.getValue();

        PsiReference[] psiReferences = ReferenceUtils.INSTANCE.convert(collectionValue, collectionAttrValue);

        PsiElement resolve;
        if (psiReferences.length == 1) {
            resolve = psiReferences[0].resolve();
        } else {
            resolve = psiReferences[psiReferences.length - 2].resolve();
        }

        PsiType psiType;
        if (resolve instanceof PsiParameter psiParameter) {
            psiType = psiParameter.getType();
        } else if (resolve instanceof PsiField psiField) {
            psiType = psiField.getType();
        } else {
            return null;
        }

        val paramPsiCls = PsiUtils.INSTANCE.resolvePsiType(psiType);

        val paramIsCollection = InheritanceUtil.isInheritor(paramPsiCls, "java.util.Collection");
        if (!paramIsCollection) {
            return null;
        }

        val genericParameters = ((PsiClassReferenceType) psiType).getParameters();
        if (genericParameters.length == 0) {
            return null;
        }

        return PsiUtils.INSTANCE.resolvePsiType(genericParameters[0]);
    }

    public static @Nullable PsiElement resolveGenericIfForeachTagItemAttrValue(PsiElement injectionHost) {
        XmlAttributeValue collectionAttrValue = getCollectionAttrIfForeachTagItemAttrValue(injectionHost);
        if (collectionAttrValue == null) {
            return null;
        }

        return resolveGenericIfForeachTagItemAttrValue(collectionAttrValue);
    }

    public static @Nullable XmlAttributeValue getItemAttrValueIfParamInForeachTag(PsiElement injectionHost){
        PsiElement parent = injectionHost.getParent();
        if (!(parent instanceof XmlTag xmlTag)) {
            return null;
        }

        String tagName = xmlTag.getName();
        if (!"foreach".equals(tagName)) {
            return null;
        }

        XmlAttribute itemAttr = xmlTag.getAttribute("item");
        if (itemAttr == null) {
            return null;
        }

        return itemAttr.getValueElement();
    }

    public static PsiElement resolveIfParamInForeachTag(MybatisFieldNameReference mybatisFieldNameReference,
                                                         PsiElement injectionHost,
                                                         PsiElement currentPsiElement) {
        PsiElement parent = injectionHost.getParent();
        if (!(parent instanceof XmlTag xmlTag)) {
            return null;
        }

        String tagName = xmlTag.getName();
        if (!"foreach".equals(tagName)) {
            return null;
        }

        PsiReference[] references = currentPsiElement.getReferences();
        if (references.length == 0) {
            return null;
        }

        PsiReference firstPsiReference = references[0];
        if (!(firstPsiReference instanceof MybatisFieldNameReference firstMybatisFieldNameReference)) {
            return null;
        }

        XmlAttribute itemAttr = xmlTag.getAttribute("item");
        if (itemAttr == null) {
            return null;
        }

        String text = currentPsiElement.getText();

        PsiElement itemPsiElement = null;
        if (firstMybatisFieldNameReference.getFrom().substring(text).equals(itemAttr.getValue())) {
            XmlAttribute collectionAttr = xmlTag.getAttribute("collection");
            if (collectionAttr == null) {
                return null;
            }

            XmlAttributeValue collectionAttrValue = collectionAttr.getValueElement();
            if (collectionAttrValue == null) {
                return null;
            }

            itemPsiElement = resolveGenericIfForeachTagItemAttrValue(collectionAttrValue);

            if (firstMybatisFieldNameReference == mybatisFieldNameReference) {
                return itemPsiElement;
            }
        }

        if (itemPsiElement == null) {
            return null;
        }

        if (references.length == 2 && references[1] == mybatisFieldNameReference) {
            //noinspection ConstantValue
            if (!(itemPsiElement instanceof PsiClass itemPsiCls)) {
                return null;
            }

            String fieldName = mybatisFieldNameReference.getFrom().substring(text);

            return itemPsiCls.findFieldByName(fieldName, true);
        }

        return null;
    }

    public static PsiElement findReference(MybatisFieldNameReference mybatisFieldNameReference, PsiMethod psiMethod,
                                           PsiElement injectionHost, PsiElement currentPsiElement,
                                           LinkedList<String> nameLevels, Project project) {
        PsiElement itemPsiElement = resolveGenericIfForeachTagItemAttrValue(injectionHost);
        if (itemPsiElement != null) {
            return itemPsiElement;
        }

        itemPsiElement = resolveIfParamInForeachTag(mybatisFieldNameReference, injectionHost, currentPsiElement);
        if (itemPsiElement != null) {
            return itemPsiElement;
        }

        PsiParameterList parameterList = psiMethod.getParameterList();
        if (parameterList.isEmpty()) {
            return null;
        }

        PsiParameter psiParameter = getPsiParameter(parameterList, nameLevels, project);
        if (psiParameter == null) {
            return null;
        }

        if (nameLevels.isEmpty()) {
            return psiParameter;
        }

        PsiType paramPsiType = psiParameter.getType();
        val paramPsiCls = PsiUtils.INSTANCE.resolvePsiType(paramPsiType);
        if (paramPsiCls == null) {
            return null;
        }

        return PsiUtils.INSTANCE.resolveTarget(paramPsiCls, nameLevels);
    }

    public static @Nullable PsiParameter getPsiParameter(PsiParameterList parameterList, LinkedList<String> nameLevel, Project project) {
        if (nameLevel.isEmpty()) {
            return null;
        }

        PsiConstantEvaluationHelper helper = JavaPsiFacade.getInstance(project).getConstantEvaluationHelper();

        String name = nameLevel.pop();
        int parametersCount = parameterList.getParametersCount();
        PsiParameter[] parameters = parameterList.getParameters();

        for (PsiParameter psiParameter : parameters) {
            PsiAnnotation annotation = psiParameter.getAnnotation(Annotation.PARAM.getQualifiedName());
            if (annotation != null) {
                PsiAnnotationMemberValue paramAnnotationValue = annotation.findAttributeValue("value");
                String paramValue = (String) helper.computeConstantExpression(paramAnnotationValue);
                // 设置了 @Param 但是内容是空, 这种情况不予处理
                if (paramValue == null || paramValue.isBlank()) {
                    continue;
                }

                if (paramValue.equals(name)) {
                    return psiParameter;
                }
            } else {
                // @Param 注解一定为空的情况下的跳转处理
                // 兼容java8开启 -parameters 参数的方式，这种方式不需要 @Param 参数
                if (Objects.equals(psiParameter.getName(), name)) {
                    return psiParameter;
                }

                if (parametersCount == 1) {
                    nameLevel.push(name);
                    return psiParameter;
                }
            }
        }

        return null;
    }

}
