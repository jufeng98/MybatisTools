package com.baomidou.plugin.idea.mybatisx.reference;

import com.baomidou.plugin.idea.mybatisx.dom.MapperBacktrackingUtils;
import com.baomidou.plugin.idea.mybatisx.dom.converter.PropertySetterFind;
import com.baomidou.plugin.idea.mybatisx.service.JavaService;
import com.baomidou.plugin.idea.mybatisx.util.MybatisConstants;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.util.PropertyUtil;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.util.IncorrectOperationException;
import lombok.Getter;
import lombok.Setter;
import org.javamaster.mybatis.tools.utils.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * The type Context psi field reference.
 *
 * @author yanglin
 */
@Setter
@Getter
public class ContextPsiFieldReference extends PsiReferenceBase<XmlAttributeValue> {

    /**
     * The Resolver.
     * -- GETTER --
     * Gets resolver.
     * <p>
     * <p>
     * -- SETTER --
     * Sets resolver.
     */
    protected ContextReferenceSetResolver<XmlAttributeValue, PsiField> resolver;

    /**
     * The Index.
     * -- GETTER --
     * Gets index.
     * <p>
     * <p>
     * -- SETTER --
     * Sets index.
     */
    protected int index;

    /**
     * Instantiates a new Context psi field reference.
     *
     * @param element the element
     * @param range   the range
     * @param index   the index
     */
    public ContextPsiFieldReference(XmlAttributeValue element, TextRange range, int index) {
        super(element, range, false);
        this.index = index;
        resolver = ReferenceSetResolverFactory.createPsiFieldResolver(element);
    }

    @Override
    public PsiElement handleElementRename(@NotNull String newElementName) throws IncorrectOperationException {
        final XmlAttributeValue element = getElement();

        newElementName = StringUtil.getPropertyNameByGetter(newElementName);

        final PsiElement parent = element.getParent();
        if (parent instanceof XmlAttribute xmlAttribute) {
            xmlAttribute.setValue(newElementName);
        }

        return element;
    }

    @Override
    public boolean isReferenceTo(@NotNull PsiElement element) {
        PsiElement resolved = resolve();

        if (resolved == element) {
            return true;
        }

        if (resolved instanceof PsiField resolvedField) {
            if (!(element instanceof PsiMethod psiMethod)) {
                return false;
            }

            if (!PropertyUtil.isSimpleGetter(psiMethod)) {
                return false;
            }

            String propertyName = PropertyUtil.getPropertyName(psiMethod);
            if (!resolvedField.getName().equals(propertyName)) {
                return false;
            }

            PsiClass resolvedClass = PsiTreeUtil.getParentOfType(resolvedField, PsiClass.class);
            PsiClass targetClass = PsiTreeUtil.getParentOfType(element, PsiClass.class);

            return resolvedClass == targetClass;
        }

        return false;
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        Optional<PsiField> resolved = resolver.resolve(index);
        if (resolved.isPresent()) {
            return resolved.get();
        }

        final Optional<PsiClass> targetClazz = getTargetClazz();
        return targetClazz.orElse(null);
    }


    @Override
    public Object @NotNull [] getVariants() {
        Optional<PsiClass> clazz = getTargetClazz();
        if (clazz.isEmpty()) {
            return PsiReference.EMPTY_ARRAY;
        }

        final PsiClass psiClass = clazz.get();
        PropertySetterFind propertySetterFind = new PropertySetterFind();

        final List<PsiField> setterFields = propertySetterFind.getSetterFields(psiClass);
        return setterFields.toArray(new Object[0]);
    }

    private Optional<PsiClass> getTargetClazz() {
        if (getElement().getValue().contains(MybatisConstants.DOT_SEPARATOR)) {
            int ind = 0 == index ? 0 : index - 1;
            Optional<PsiField> resolved = resolver.resolve(ind);
            if (resolved.isPresent()) {
                return JavaService.getInstance(myElement.getProject()).getReferenceClazzOfPsiField(resolved.get());
            }
        } else {
            return MapperBacktrackingUtils.getPropertyClazz(myElement);
        }

        return Optional.empty();
    }

}