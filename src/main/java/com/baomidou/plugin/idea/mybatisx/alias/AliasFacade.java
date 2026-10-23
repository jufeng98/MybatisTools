package com.baomidou.plugin.idea.mybatisx.alias;


import com.baomidou.plugin.idea.mybatisx.model.AliasDesc;
import com.baomidou.plugin.idea.mybatisx.util.MybatisConstants;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * The type Alias facade.
 *
 * @author yanglin
 */
public class AliasFacade {

    private final Project project;

    private final JavaPsiFacade javaPsiFacade;

    private final List<AliasResolver> resolvers;

    /**
     * Instantiates a new Alias facade.
     *
     * @param project the project
     */
    public AliasFacade(Project project) {
        this.project = project;
        this.resolvers = new ArrayList<>();
        this.javaPsiFacade = JavaPsiFacade.getInstance(project);

        initResolvers();
    }

    /**
     * Gets instance.
     *
     * @param project the project
     * @return the instance
     */
    public static AliasFacade getInstance(@NotNull Project project) {
        return project.getService(AliasFacade.class);
    }

    private void initResolvers() {
        try {
            Class.forName("com.intellij.spring.model.utils.SpringModelUtils");

            this.registerResolver(AliasResolverFactory.createBeanResolver(project));

            // support springboot alias
            this.registerResolver(AliasResolverFactory.createSpringBootResolver(project));
        } catch (ClassNotFoundException ignored) {
        }

        this.registerResolver(AliasResolverFactory.createSingleAliasResolver(project));
        this.registerResolver(AliasResolverFactory.createConfigPackageResolver(project));
        this.registerResolver(AliasResolverFactory.createAnnotationResolver(project));
        this.registerResolver(AliasResolverFactory.createInnerAliasResolver(project));
    }

    /**
     * Find psi class optional.
     *
     * @param element   the element
     * @param shortName the short name
     * @return the optional
     */
    public Optional<PsiClass> findPsiClass(@Nullable PsiElement element, @NotNull String shortName) {
        // 查找mybatis内部支持的引用类型, 支持跳转到引用类
        String fullName = InternalAlias.referenceFullName(shortName);
        if (fullName == null) {
            fullName = shortName;
        }

        PsiClass clazz = javaPsiFacade.findClass(fullName, GlobalSearchScope.allScope(project));
        if (null != clazz) {
            return Optional.of(clazz);
        }

        for (AliasResolver resolver : resolvers) {
            for (AliasDesc desc : resolver.getClassAliasDescriptions(element)) {
                if (desc != null && desc.getAlias().equalsIgnoreCase(fullName)) {
                    return Optional.of(desc.getClazz());
                }
            }
        }

        return Optional.empty();
    }

    /**
     * Gets alias descs.
     *
     * @param element the element
     * @return the alias descs
     */
    @NotNull
    public Collection<AliasDesc> getAliasDescs(@Nullable PsiElement element) {
        ArrayList<AliasDesc> result = new ArrayList<>();

        for (AliasResolver resolver : resolvers) {
            result.addAll(resolver.getClassAliasDescriptions(element));
        }

        return result;
    }

    public List<LookupElementBuilder> getAliasElements(@Nullable PsiElement element) {
        Collection<AliasDesc> aliasDescs = getAliasDescs(element);

        return aliasDescs.stream()
                .map(it -> {
                    PsiClass psiClass = it.getClazz();

                    return LookupElementBuilder
                            .create(psiClass, it.getAlias())
                            .withTypeText(psiClass.getQualifiedName());
                })
                .toList();
    }

    /**
     * Register resolver.
     *
     * @param resolver the resolver
     */
    public void registerResolver(@NotNull AliasResolver resolver) {
        this.resolvers.add(resolver);
    }

    private static class InternalAlias {

        private static volatile Map<String, String> refMap = null;

        private static Map<String, String> getRefMap() {
            if (refMap == null) {
                synchronized (InternalAlias.class) {
                    if (refMap == null) {
                        InternalAlias.refMap = buildRefMap();
                    }
                }
            }
            return refMap;
        }

        private static Map<String, String> buildRefMap() {
            Map<String, String> refMap = new HashMap<>();
            /* 数组不跳转 基本类型*/
            refMap.put("_byte[]", "java.lang.Byte");
            refMap.put("_long[]", "java.lang.Long");
            refMap.put("_short[]", "java.lang.Short");
            refMap.put("_int[]", MybatisConstants.INTEGER);
            refMap.put("_integer[]", MybatisConstants.INTEGER);
            refMap.put("_double[]", "java.lang.Double");
            refMap.put("_float[]", "java.lang.Float");
            refMap.put("_boolean[]", "java.lang.Boolean");
            /* 数组不跳转 引用类型类型*/
            refMap.put("byte[]", "java.lang.Byte");
            refMap.put("long[]", "java.lang.Long");
            refMap.put("short[]", "java.lang.Short");
            refMap.put("int[]", MybatisConstants.INTEGER);
            refMap.put("integer[]", MybatisConstants.INTEGER);
            refMap.put("double[]", "java.lang.Double");
            refMap.put("float[]", "java.lang.Float");
            refMap.put("boolean[]", "java.lang.Boolean");
            /* 数组不跳转 默认引用类型*/
            refMap.put("date[]", "java.util.Date");
            refMap.put("decimal[]", "java.math.BigDecimal");
            refMap.put("bigdecimal[]", "java.math.BigDecimal");
            refMap.put("biginteger[]", "java.math.BigInteger");
            refMap.put("object[]", "java.lang.Object");
            // 基本类型映射的引用类型
            refMap.put("_byte", "java.lang.Byte");
            refMap.put("_long", "java.lang.Long");
            refMap.put("_short", "java.lang.Short");
            refMap.put("_int", MybatisConstants.INTEGER);
            refMap.put("_integer", MybatisConstants.INTEGER);
            refMap.put("_double", "java.lang.Double");
            refMap.put("_float", "java.lang.Float");
            refMap.put("_boolean", "java.lang.Boolean");
            // 普通引用类型
            refMap.put("string", "java.lang.String");
            refMap.put("byte", "java.lang.Byte");
            refMap.put("long", "java.lang.Long");
            refMap.put("short", "java.lang.Short");
            refMap.put("int", MybatisConstants.INTEGER);
            refMap.put("integer", MybatisConstants.INTEGER);
            refMap.put("double", "java.lang.Double");
            refMap.put("float", "java.lang.Float");
            refMap.put("boolean", "java.lang.Boolean");
            refMap.put("date", "java.util.Date");
            refMap.put("decimal", "java.math.BigDecimal");
            refMap.put("bigdecimal", "java.math.BigDecimal");
            refMap.put("biginteger", "java.math.BigInteger");
            refMap.put("object", "java.lang.Object");
            refMap.put("map", MybatisConstants.MAP);
            refMap.put("hashmap", "java.util.HashMap");
            refMap.put("list", "java.util.List");
            refMap.put("arraylist", "java.util.ArrayList");
            refMap.put("collection", MybatisConstants.COLLECTION);
            refMap.put("iterator", "java.util.Iterator");
            refMap.put("resultset", "java.sql.ResultSet");
            return refMap;
        }

        public static String referenceFullName(String key) {
            return getRefMap().get(key.toLowerCase(Locale.ENGLISH));
        }
    }
}
