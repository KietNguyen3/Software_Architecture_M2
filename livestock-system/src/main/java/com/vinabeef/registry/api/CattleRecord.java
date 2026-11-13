package com.vinabeef.registry.api;

public record CattleRecord(
        long id,
        String animalCode,
        String rfidTag,
        String breed,
        long expectedBarnId,
        String expectedBarnCode,
        String status,
        boolean active) {
}
