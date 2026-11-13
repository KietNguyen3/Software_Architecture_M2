package com.vinabeef.registry.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

/**
 * An animal and its permanent RFID ear tag (FR-01, FR-08). The unique constraint on the tag
 * and optimistic locking enforce integrity in the database, not only in code (P6, AD-2).
 */
@Entity
@Table(name = "cattle")
public class Cattle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String animalCode;

    @Column(nullable = false, unique = true, length = 15)
    private String rfidTag;

    private String breed;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Barn expectedBarn;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CattleStatus status = CattleStatus.ACTIVE;

    @Version
    private long version;

    protected Cattle() {
    }

    public Cattle(String animalCode, String rfidTag, String breed, Barn expectedBarn) {
        this.animalCode = animalCode;
        this.rfidTag = rfidTag;
        this.breed = breed;
        this.expectedBarn = expectedBarn;
    }

    public void changeStatus(CattleStatus newStatus) {
        if (newStatus == null) {
            throw new IllegalArgumentException("Status is required");
        }
        this.status = newStatus;
    }

    public boolean isActive() {
        return status == CattleStatus.ACTIVE;
    }

    public Long getId() {
        return id;
    }

    public String getAnimalCode() {
        return animalCode;
    }

    public String getRfidTag() {
        return rfidTag;
    }

    public String getBreed() {
        return breed;
    }

    public Barn getExpectedBarn() {
        return expectedBarn;
    }

    public CattleStatus getStatus() {
        return status;
    }
}
