package com.baomidou.plugin.idea.mybatisx.dom.converter;

import com.baomidou.plugin.idea.mybatisx.dom.model.IdDomElement;
import com.baomidou.plugin.idea.mybatisx.dom.model.Mapper;
import com.baomidou.plugin.idea.mybatisx.util.MapperUtils;
import com.baomidou.plugin.idea.mybatisx.util.MybatisConstants;
import com.google.common.collect.Lists;
import com.intellij.codeInsight.completion.PrioritizedLookupElement;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.JavaClassReferenceProvider;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlElement;
import com.intellij.util.xml.*;
import lombok.val;
import org.javamaster.mybatis.tools.service.IdDomElementService;
import org.jetbrains.annotations.*;

import java.util.*;

/**
 * The type Id based tag converter.
 *
 * @author yanglin
 */
public abstract class IdBasedTagConverter extends ConverterAdaptor<XmlAttributeValue> implements CustomReferenceConverter<XmlAttributeValue> {

    private final boolean crossMapperSupported;

    /**
     * Instantiates a new Id based tag converter.
     */
    public IdBasedTagConverter() {
        this(true);
    }

    /**
     * Instantiates a new Id based tag converter.
     *
     * @param crossMapperSupported the cross mapper supported
     */
    protected IdBasedTagConverter(boolean crossMapperSupported) {
        this.crossMapperSupported = crossMapperSupported;
    }

    @Nullable
    @Override
    public XmlAttributeValue fromString(@Nullable @NonNls String value, @NotNull ConvertContext context) {
        TraverseStrategy traverseStrategy = selectStrategy(context);

        val domMap = traverseStrategy.getValue();

        return matchIdDomElement(domMap, value, context).orElse(null);
    }

    private Optional<XmlAttributeValue> matchIdDomElement(Map<String, Collection<? extends IdDomElement>> domMap,
                                                          String value, ConvertContext context) {
        if (value == null) {
            return Optional.empty();
        }

        String ns;
        String currentId;
        int dotIdx = value.lastIndexOf(MybatisConstants.DOT_SEPARATOR);
        if (dotIdx != -1) {
            ns = value.substring(0, dotIdx);
            currentId = value.substring(Math.min(dotIdx + 1, value.length()));
        } else {
            Mapper contextMapper = MapperUtils.getMapper(context.getInvocationElement());

            ns = MapperUtils.getNamespace(contextMapper);
            currentId = value;
        }

        val domElements = domMap.getOrDefault(ns, Collections.emptyList());

        val optional = domElements.stream()
                .filter(it -> {
                    String id = MapperUtils.getId(it);
                    return Objects.equals(id, currentId);
                })
                .findFirst();

        return optional.map(idDomElement -> idDomElement.getId().getXmlAttributeValue());
    }

    @Nullable
    @Override
    public String toString(@Nullable XmlAttributeValue xmlAttribute, @NotNull ConvertContext context) {
        if (xmlAttribute == null) {
            return null;
        }

        PsiElement fatherElement = xmlAttribute.getParent();
        if (fatherElement == null) {
            return null;
        }

        DomElement domElement = DomUtil.getDomElement(fatherElement.getParent());
        if (!(domElement instanceof IdDomElement)) {
            return null;
        }

        Mapper contextMapper = MapperUtils.getMapper(context.getInvocationElement());
        return MapperUtils.getIdSignature((IdDomElement) domElement, contextMapper);

    }

    private TraverseStrategy selectStrategy(ConvertContext context) {
        return crossMapperSupported ? new CrossMapperStrategy(context) : new InsideMapperStrategy(context);
    }

    /**
     * Gets comparisons.
     *
     * @param mapper mapper in the project, null if {@link #crossMapperSupported} is false
     * @return the comparisons
     */
    @NotNull
    public abstract Map<String, Collection<? extends IdDomElement>> getComparisons(@Nullable Mapper mapper);

    public boolean isContextResultMapSelf(IdDomElement domElement, ConvertContext context) {
        return false;
    }

    @Override
    public PsiReference @NotNull [] createReferences(GenericDomValue<XmlAttributeValue> value,
                                                     PsiElement element,
                                                     ConvertContext context) {
        ValueReferenceProvider valueReferenceProvider = new ValueReferenceProvider(context);

        return PsiClassConverter.createJavaClassReferenceProvider(value, null, valueReferenceProvider)
                .getReferencesByElement(element);
    }

    private abstract static class TraverseStrategy {
        /**
         * The Context.
         */
        protected final ConvertContext context;

        /**
         * Instantiates a new Traverse strategy.
         *
         * @param context the context
         */
        public TraverseStrategy(@NotNull ConvertContext context) {
            this.context = context;
        }

        /**
         * Gets value.
         *
         * @return the value
         */
        public abstract Map<String, Collection<? extends IdDomElement>> getValue();
    }

    private class InsideMapperStrategy extends TraverseStrategy {

        /**
         * Instantiates a new Inside mapper strategy.
         *
         * @param context the context
         */
        public InsideMapperStrategy(@NotNull ConvertContext context) {
            super(context);
        }

        @Override
        public Map<String, Collection<? extends IdDomElement>> getValue() {
            return getComparisons(null);
        }

    }

    private class CrossMapperStrategy extends TraverseStrategy {

        /**
         * Instantiates a new Cross mapper strategy.
         *
         * @param context the context
         */
        public CrossMapperStrategy(@NotNull ConvertContext context) {
            super(context);
        }

        @Override
        public Map<String, Collection<? extends IdDomElement>> getValue() {
            Project project = context.getProject();
            Module module = ModuleUtilCore.findModuleForPsiElement(context.getFile());

            val idDomElementService = project.getService(IdDomElementService.class);

            return idDomElementService.getCacheDomElementMap(module, IdBasedTagConverter.this);
        }
    }

    private class ValueReferenceProvider extends JavaClassReferenceProvider {

        private final ConvertContext context;

        private ValueReferenceProvider(ConvertContext context) {
            this.context = context;
        }

        @Nullable
        @Override
        public GlobalSearchScope getScope(@NotNull Project project) {
            return GlobalSearchScope.allScope(project);
        }

        /**
         * It looks like hacking here, as it's a little hard to handle so many different cases as JetBrains does
         */

        @Override
        public PsiReference @NotNull [] getReferencesByString(String text, @NotNull PsiElement position, int offsetInPosition) {
            List<PsiReference> refs = Lists.newArrayList(super.getReferencesByString(text, position, offsetInPosition));

            ValueReference vr = new ValueReference(position, getTextRange(position), context, text);

            if (!refs.isEmpty() && 0 != vr.getVariants().length) {
                refs.remove(refs.size() - 1);
                refs.add(vr);
            }

            return refs.toArray(new PsiReference[0]);
        }

        private TextRange getTextRange(PsiElement element) {
            String text = element.getText();

            int index = text.lastIndexOf(MybatisConstants.DOT_SEPARATOR);

            return -1 == index ? ElementManipulators.getValueTextRange(element) :
                    TextRange.create(text.substring(0, index).length() + 1, text.length() - 1);
        }
    }

    private class ValueReference extends PsiReferenceBase<PsiElement> {

        private final ConvertContext context;
        private final String text;

        /**
         * Instantiates a new Value reference.
         *
         * @param element the element
         * @param rng     the rng
         * @param context the context
         * @param text    the text
         */
        public ValueReference(@NotNull PsiElement element, TextRange rng, ConvertContext context, String text) {
            super(element, rng, false);
            this.context = context;
            this.text = text;
        }

        @Nullable
        @Override
        public PsiElement resolve() {
            return IdBasedTagConverter.this.fromString(text, context);
        }


        @Override
        public Object @NotNull [] getVariants() {
            TraverseStrategy traverseStrategy = selectStrategy(context);

            val newMap = new HashMap<>(traverseStrategy.getValue());

            List<LookupElement> result = setupGlobalIdSignature(newMap);

            return result.toArray(new LookupElement[0]);
        }

        private List<LookupElement> setupContextIdSignature(@Nullable Collection<? extends IdDomElement> domElements,
                                                            boolean onlyId, String ns) {
            if (domElements == null) {
                return Collections.emptyList();
            }

            return domElements.stream()
                    .map(it -> {
                        String id = MapperUtils.getId(it);
                        if (id == null) {
                            return null;
                        }

                        XmlElement xmlElement = it.getXmlElement();
                        if (xmlElement == null) {
                            return null;
                        }

                        String str = onlyId ? id : ns + "." + id;
                        return PrioritizedLookupElement.withPriority(
                                LookupElementBuilder.create(xmlElement, str),
                                onlyId ? 200 : 100
                        );
                    })
                    .filter(Objects::nonNull)
                    .toList();
        }

        private List<LookupElement> setupGlobalIdSignature(Map<String, Collection<? extends IdDomElement>> domMap) {
            Mapper contextMapper = MapperUtils.getMapper(context.getInvocationElement());

            String contextNs = MapperUtils.getNamespace(contextMapper);
            val contextElements = domMap.remove(contextNs);
            if (contextElements == null) {
                return Collections.emptyList();
            }

            List<? extends IdDomElement> elements = contextElements.stream()
                    .filter(it -> !isContextResultMapSelf(it, context))
                    .toList();

            List<LookupElement> list1 = setupContextIdSignature(elements, true, contextNs);

            List<LookupElement> list2 = domMap.entrySet().stream()
                    .map(it -> {
                        String ns = it.getKey();
                        val list = it.getValue();

                        return setupContextIdSignature(list, false, ns);
                    })
                    .flatMap(Collection::stream)
                    .toList();

            List<LookupElement> result = new ArrayList<>(list1.size() + list2.size());
            result.addAll(list1);
            result.addAll(list2);

            return result;
        }

    }
}
