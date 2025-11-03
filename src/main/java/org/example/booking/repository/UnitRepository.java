package org.example.booking.repository;

import jakarta.persistence.LockModeType;
import org.example.booking.model.Unit;
import org.example.booking.repository.base.SliceableRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UnitRepository extends JpaRepository<Unit, Long>, SliceableRepository<Unit> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM Unit u WHERE u.id = :id")
    Optional<Unit> findByIdWithLock(Long id);

    int countByDescriptionEquals(String description);
}
