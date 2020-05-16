package org.ssssssss.magicapi.provider.impl;

import org.ssssssss.magicapi.provider.KeyProvider;

import java.util.UUID;

public class UUIDKeyProvider implements KeyProvider {

    @Override
    public String getName() {
        return "uuid";
    }

    @Override
    public Object getKey() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
