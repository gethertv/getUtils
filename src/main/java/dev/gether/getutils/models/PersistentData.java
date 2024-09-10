package dev.gether.getutils.models;

import org.bukkit.persistence.PersistentDataType;

public final class PersistentData {


    public static final PersistentDataType<?, ?>[] TYPES = {
            PersistentDataType.BYTE, PersistentDataType.SHORT, PersistentDataType.INTEGER,
            PersistentDataType.LONG, PersistentDataType.FLOAT, PersistentDataType.DOUBLE,
            PersistentDataType.STRING, PersistentDataType.BYTE_ARRAY, PersistentDataType.INTEGER_ARRAY,
            PersistentDataType.LONG_ARRAY, PersistentDataType.TAG_CONTAINER_ARRAY, PersistentDataType.TAG_CONTAINER
    };

}
