package org.example.booking.repository;

import org.example.booking.model.UnitEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UnitEventRepository extends JpaRepository<UnitEvent, Long> {
}