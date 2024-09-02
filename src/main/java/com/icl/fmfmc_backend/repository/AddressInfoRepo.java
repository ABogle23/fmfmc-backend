package com.icl.fmfmc_backend.repository;

import com.icl.fmfmc_backend.entity.AddressInfo;
import org.springframework.data.jpa.repository.JpaRepository;

@Deprecated
public interface AddressInfoRepo extends JpaRepository<AddressInfo, String> {
}