package com.intellij.lang.ognl;

import com.intellij.openapi.util.IconLoader;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public final class OgnlIcons {
    /**
     * 10x10
     */
    public static final @NotNull Icon Action_small = load();

    private static @NotNull Icon load() {
        return IconLoader.getIcon("icons/action_small.svg", OgnlIcons.class.getClassLoader());
    }
}