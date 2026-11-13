package com.vinabeef.registry.domain;

import com.vinabeef.shared.contract.GateType;
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

/** A barn gate equipped with one RFID reader station (inner and outer antenna). */
@Entity
@Table(name = "gate")
public class Gate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GateType type;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Barn barn;

    protected Gate() {
    }

    public Gate(String code, String name, GateType type, Barn barn) {
        this.code = code;
        this.name = name;
        this.type = type;
        this.barn = barn;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public GateType getType() {
        return type;
    }

    public Barn getBarn() {
        return barn;
    }
}
