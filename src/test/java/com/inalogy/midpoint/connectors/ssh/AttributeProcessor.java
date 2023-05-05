package com.inalogy.midpoint.connectors.ssh;


import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeBuilder;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AttributeProcessor {
    protected AttributeProcessor() {

    }

    protected static Set<Attribute> getTestAttributeSet() {
        Set<Attribute> attributes = new HashSet<>();

        Attribute attr1 = AttributeBuilder.build("name", "vaclav");
        Attribute attr2 = AttributeBuilder.build("type", Stream.of("user","person").collect(Collectors.toList()));

        attributes.add(attr1);
        attributes.add(attr2);

        return attributes;
    }
}