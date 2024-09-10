package com.icl.fmfmc_backend.entity.foodEstablishment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Category object containing food establishment category information")
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Category {
    private Long categoryId;
    private String name;
}
