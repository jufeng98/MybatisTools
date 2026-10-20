package com.baomidou.plugin.idea.mybatisx.dom.converter;

import com.baomidou.plugin.idea.mybatisx.dom.model.IdDomElement;
import com.baomidou.plugin.idea.mybatisx.dom.model.Mapper;
import com.baomidou.plugin.idea.mybatisx.util.MapperUtils;
import com.google.common.collect.Maps;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * The type Parameter map converter.
 *
 * @author yanglin
 */
public class ParameterMapConverter extends IdBasedTagConverter {

    @NotNull
    @Override
    public Map<String, Collection<? extends IdDomElement>> getComparisons(@Nullable Mapper mapper) {
        if (mapper == null) {
            return Collections.emptyMap();
        }

        Map<String, Collection<? extends IdDomElement>> map = Maps.newHashMap();

        String namespace = MapperUtils.getNamespace(mapper);

        map.put(namespace, mapper.getParameterMaps());

        return map;
    }

}
