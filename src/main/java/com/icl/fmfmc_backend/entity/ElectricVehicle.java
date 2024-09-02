package com.icl.fmfmc_backend.entity;


import com.icl.fmfmc_backend.entity.enums.ConnectionType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@Entity
@Data
@Table(name = "electric_vehicles")
public class ElectricVehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @NotNull
    @Column(nullable = false)
    private String model;
    @NotNull
    @Column(nullable = false)
    private String brand;
    @NotNull
    private Double batteryCapacity; // in kWh
    @NotNull
    private Double pricePerMile; // will be converted and stored as price per km
    @NotNull
    private Double topSpeed; // in km/h
    @NotNull
    private Double evRange; // in km
    @NotNull
    private Double efficiency; // in Wh/km
    @NotNull
    private Double rapidChargeSpeed; // will be stored as is, no conversion required
    @NotEmpty
    @ElementCollection(fetch = FetchType.LAZY)
    @Enumerated(EnumType.STRING)
    private List<ConnectionType> chargePortTypes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


    // convert from miles to km
    public void convertMilesToKm() {
        if (this.topSpeed != null) {
            this.topSpeed *= 1.60934; // Convert mph to km/h
        }
        if (this.evRange != null) {
            this.evRange *= 1.60934; // Convert miles to km
        }
        if (this.efficiency != null) {
            this.efficiency *= 0.621371; // Convert Wh/mi to Wh/km (inverse since smaller number means more efficient)
        }
    }

    @PrePersist
    @PreUpdate // Ensure conversion happens on updates as well
    public void onPreSave() {
        convertMilesToKm(); // Automatically convert before saving or updating the database
    }

}