package com.icl.fmfmc_backend.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "address_info")
public class AddressInfo {

    // TODO: Add more way more fields

//  @Id
//  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "address_info_seq")
//  @SequenceGenerator(name = "address_info_seq", sequenceName = "address_info_seq", allocationSize = 1)
//  private Long id;

    @Id
    private String id;
  //    private String title;
  private String formatedAddress;
  //  private String addressLine1;
  //  private String addressLine2;
  private String town;
  private String postcode;
  //  private String stateOrProvince;
  //  private Double latitude;
  //  private Double longitude;
  //  private Long countryID;
  //  private String contactTelephone1;
  //  private String contactTelephone2;
  //  private String contactEmail;
  //  private String relatedURL;
  //  private String accessComments;
}
