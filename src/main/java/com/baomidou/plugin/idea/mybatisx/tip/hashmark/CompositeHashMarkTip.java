package com.baomidou.plugin.idea.mybatisx.tip.hashmark;

import com.baomidou.plugin.idea.mybatisx.annotation.Annotation;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.javamaster.mybatis.tools.utils.PsiUtils;
import org.jetbrains.annotations.Nullable;

import java.util.*;

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

    public static PsiElement findReference(PsiMethod psiMethod, LinkedList<String> nameLevels, Project project) {
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
