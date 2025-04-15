package dev.gether.getutils.adapter;

import com.fasterxml.jackson.databind.module.SimpleModule;

public interface ServerSerializer {
    void registerSerializers(SimpleModule module);
    void registerDeserializers(SimpleModule module);
}