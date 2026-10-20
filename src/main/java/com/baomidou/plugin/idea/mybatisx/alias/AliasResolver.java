package com.baomidou.plugin.idea.mybatisx.alias;

import com.baomidou.plugin.idea.mybatisx.model.AliasDesc;
import com.baomidou.plugin.idea.mybatisx.util.JavaUtils;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * The type Alias resolver.
 *
 * @author yanglin
 */
@Setter
@Getter
public abstract class AliasResolver {

    /**
     * The Project.
     * -- GETTER --
     * Gets project.
     * <p>
     * <p>
     * -- SETTER --
     * Sets project.
     */
    protected Project project;

    /**
     * Instantiates a new Alias resolver.
     *
     * @param project the project
     */
    public AliasResolver(Project project) {
        this.project = project;
    }

    /**
     * Add alias desc optional.
     *
     * @param descs the descs
     * @param clazz the clazz
     * @param alias the alias
     */
    protected void addAliasDesc(@NotNull Set<AliasDesc> descs, @Nullable PsiClass clazz, @Nullable String alias) {
        if (null == alias || !JavaUtils.isModelClazz(clazz)) {
            return;
        }
        AliasDesc desc = new AliasDesc();
        descs.add(desc);
        desc.setClazz(clazz);
        desc.setAlias(alias);
    }

    /**
     * Gets class alias descriptions.
     *
     * @param element the element
     * @return the class alias descriptions
     */
    @NotNull
    public abstract Set<AliasDesc> getClassAliasDescriptions(@Nullable PsiElement element);

}
