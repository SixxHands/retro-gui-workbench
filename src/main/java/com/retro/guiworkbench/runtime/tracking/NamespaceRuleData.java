package com.retro.guiworkbench.runtime.tracking;

import java.util.Objects;

public record NamespaceRuleData(String fragment, String namespace)
{
    public NamespaceRuleData
    {
        Objects.requireNonNull(fragment, "fragment");
        Objects.requireNonNull(namespace, "namespace");
    }
}
