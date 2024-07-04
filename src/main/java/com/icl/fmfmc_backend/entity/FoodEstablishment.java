package com.icl.fmfmc_backend.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;
import org.locationtech.jts.geom.Point;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "food_establishment")
public class FoodEstablishment {

    @Id
    private String id;

    private String name;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "address_info_id", referencedColumnName = "id")
//    @JsonManagedReference
    private AddressInfo address;

    @ElementCollection(fetch = FetchType.LAZY)
    private List<Category> categories;

    @Embedded
    private GeoCoordinates geocodes;

    private String closedStatus;

    private Double popularity;

    private Integer price;

    private Double rating;

    private LocalDateTime createdAt;

    @Column(columnDefinition = "Point")
    private Point location;

}