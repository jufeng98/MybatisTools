package com.baomidou.plugin.idea.mybatisx.dom;

import com.baomidou.plugin.idea.mybatisx.dom.model.*;
import com.baomidou.plugin.idea.mybatisx.dom.model.Collection;
import com.baomidou.plugin.idea.mybatisx.util.JavaUtils;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.xml.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;


/**
 * The type Mapper backtracking utils.
 *
 * @author yanglin
 */
public final class MapperBacktrackingUtils {

    private MapperBacktrackingUtils() {
        throw new UnsupportedOperationException();
    }

    /**
     * Gets property clazz.
     *
     * @param attributeValue the attribute value
     * @return the property clazz
     */
    public static Optional<PsiClass> getPropertyClazz(XmlAttributeValue attributeValue) {
        DomElement domElement = DomUtil.getDomElement(attributeValue);
        if (null == domElement) {
            return Optional.empty();
        }

        List<Class<? extends DomElement>> domList = Arrays.asList(ResultMap.class, Collection.class, Association.class, ParameterMap.class);

        DomElement parent = getParent(domElement, domList);

        if (parent != null && isWithinSameTag(parent, attributeValue)) {
            parent = getParent(parent, domList);
        }

        if (parent instanceof Collection collection) {
            PsiClass collectionClass = findLeastParentType(collection, attributeValue.getProject());
            return Optional.ofNullable(collectionClass);
        }

        if (parent instanceof Association association) {
            PsiClass associationClass = findLeastParentType(association, attributeValue.getProject());
            return Optional.ofNullable(associationClass);
        }

        if (parent instanceof ResultMap resultMap) {
            return Optional.ofNullable(resultMap.getType().getValue());
        }

        if (parent instanceof ParameterMap parameterMap) {
            return Optional.ofNullable(parameterMap.getType().getValue());
        }

        return Optional.empty();
    }

    private static DomElement getParent(DomElement domElement, List<Class<? extends DomElement>> list) {
        for (DomElement cur = domElement != null ? domElement.getParent() : null;
             cur != null;
             cur = cur.getParent()
        ) {
            for (Class<? extends DomElement> aClass : list) {
                if (aClass.isInstance(cur)) {
                    return cur;
                }
            }
        }
        return null;
    }

    @Nullable
    private static PsiClass findLeastParentType(DomElement domElement, @NotNull Project project) {
        DomElement currentElement = domElement;
        PsiClass currentClass = null;
        while (true) {
            if (currentElement instanceof Association association) {
                boolean foundClass = false;
                PsiClass javaType = association.getJavaType().getValue();
                if (javaType != null) {
                    currentClass = javaType;
                    foundClass = true;
                }

                if (currentClass == null) {
                    XmlAttributeValue value = association.getProperty().getValue();
                    if (value != null) {
                        String propertyName = value.getValue();
                        PsiClass leastParentType = findLeastParentType(currentElement.getParent(), project);
                        if (leastParentType != null) {
                            Optional<PsiMember> optional = JavaUtils.findPsiMember(leastParentType, propertyName);
                            if (optional.isPresent()) {
                                PsiMember psiMember = optional.get();
                                if (psiMember instanceof PsiField psiField) {
                                    String fieldTypeClassName = psiField.getType().getCanonicalText();
                                    Optional<PsiClass> fieldClassOptional = JavaUtils.findClazz(project, fieldTypeClassName);
                                    if (fieldClassOptional.isPresent()) {
                                        currentClass = fieldClassOptional.get();
                                        foundClass = true;
                                    }
                                }
                            }
                        }
                    }
                }
                if (foundClass) {
                    break;
                }
            } else if (currentElement instanceof Collection collection) {
                boolean foundClass = false;
                GenericAttributeValue<PsiClass> type = collection.getOfType();
                if (type.getValue() != null) {
                    currentClass = type.getValue();
                    foundClass = true;
                }

                if (currentClass == null) {
                    XmlAttributeValue value = collection.getProperty().getValue();
                    if (value != null) {
                        String propertyName = value.getValue();
                        PsiClass leastParentType = findLeastParentType(currentElement.getParent(), project);
                        if (leastParentType != null) {
                            Optional<PsiMember> optional = JavaUtils.findPsiMember(leastParentType, propertyName);
                            if (optional.isPresent()) {
                                PsiMember psiMember = optional.get();
                                if (psiMember instanceof PsiField psiField) {
                                    Optional<String> genericClassOfListByField = findGenericClassOfListByField(psiField);
                                    if (genericClassOfListByField.isPresent()) {
                                        String fieldTypeClassName = genericClassOfListByField.get();
                                        Optional<PsiClass> fieldClassOptional = JavaUtils.findClazz(project, fieldTypeClassName);
                                        if (fieldClassOptional.isPresent()) {
                                            currentClass = fieldClassOptional.get();
                                            foundClass = true;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (foundClass) {
                    break;
                }
            } else if (currentElement instanceof ResultMap association) {
                GenericAttributeValue<PsiClass> type = association.getType();
                currentClass = type.getValue();
            }

            if (currentElement == null) {
                break;
            }

            currentElement = currentElement.getParent();
        }
        return currentClass;
    }

    private static Optional<String> findGenericClassOfListByField(PsiField psiField) {
        PsiTypeElement typeElement = psiField.getTypeElement();
        if (typeElement == null) {
            return Optional.empty();
        }

        PsiJavaCodeReferenceElement innermostComponentReferenceElement = typeElement.getInnermostComponentReferenceElement();
        if (innermostComponentReferenceElement == null) {
            return Optional.empty();
        }

        PsiType[] typeParameters = innermostComponentReferenceElement.getTypeParameters();
        if (typeParameters.length != 1) {
            return Optional.empty();
        }

        PsiType typeParameter = typeParameters[0];
        return Optional.of(typeParameter.getCanonicalText());
    }

    /**
     * Is within same tag boolean.
     *
     * @param domElement the dom element
     * @param xmlElement the xml element
     * @return the boolean
     */
    public static boolean isWithinSameTag(@NotNull DomElement domElement, XmlAttributeValue xmlElement) {
        XmlTag xmlTag = PsiTreeUtil.getParentOfType(xmlElement, XmlTag.class);
        return xmlTag != null && domElement.getXmlTag() != null && domElement.getXmlTag().equals(xmlTag);
    }

    public static Optional<PsiClass> getEntityClass(XmlAttributeValue attributeValue) {
        DomElement domElement = DomUtil.getDomElement(attributeValue);
        if (null == domElement) {
            return Optional.empty();
        }

        ResultMap resultMap = DomUtil.getParentOfType(domElement, ResultMap.class, true);
        if (null != resultMap && !isWithinSameTag(resultMap, attributeValue)) {
            return Optional.ofNullable(resultMap.getType().getValue());
        }

        return Optional.empty();
    }
}
