package com.baomidou.plugin.idea.mybatisx.dom.converter;

import com.intellij.util.xml.ConvertContext;
import com.intellij.util.xml.ResolvingConverter;
import org.jetbrains.annotations.*;

import java.util.Collection;
import java.util.Collections;

/**
 * The type Converter adaptor.
 *
 * @param <T> the type parameter
 * @author yanglin
 */
public abstract class ConverterAdaptor<T> extends ResolvingConverter<T> {

    @NotNull
    @Override
    public Collection<? extends T> getVariants(@NotNull ConvertContext context) {
        return Collections.emptyList();
    }

    @Nullable
    @Override
    public String toString(@Nullable T t, @NotNull ConvertContext context) {
        return null;
    }

    @Nullable
    @Override
    public T fromString(@Nullable @NonNls String s, @NotNull ConvertContext context) {
        return null;
    }
}
