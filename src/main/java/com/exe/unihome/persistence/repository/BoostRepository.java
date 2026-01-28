package com.exe.unihome.persistence.repository;

import com.exe.unihome.persistence.entity.subscription.Boost;
import com.exe.unihome.persistence.entity.subscription.BoostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoostRepository extends JpaRepository<Boost, String> {

  Page<Boost> findByStatus(BoostStatus status, Pageable pageable);
}

