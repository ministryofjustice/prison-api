package net.syscon.elite.service;

import java.util.function.Supplier;

public class EntityNotFoundException extends RuntimeException implements Supplier<EntityNotFoundException> {
    public EntityNotFoundException(String id) {
        super(id);
    }

    @Override
    public EntityNotFoundException get() {
        return new EntityNotFoundException(getMessage());
    }
}
