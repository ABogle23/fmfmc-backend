package com.icl.fmfmc_backend.entity.foodEstablishment;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.icl.fmfmc_backend.entity.AddressInfo;
import com.icl.fmfmc_backend.entity.GeoCoordinates;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.Point;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Food Establishment object containing food establishment information")
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
    private String website;
    @JsonIgnore
    @Column(columnDefinition = "Point")
    private Point location;
    private Long adjacentChargerId;

}