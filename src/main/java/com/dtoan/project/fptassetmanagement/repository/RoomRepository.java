package com.dtoan.project.fptassetmanagement.repository;

import com.dtoan.project.fptassetmanagement.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    List<Room> findByIsActiveTrueOrderByCodeAsc();
    boolean existsByCode(String code);

    Room findByCode(String code);

    @Query("""
            SELECT COUNT(r) > 0
            FROM Room r
            WHERE UPPER(TRIM(r.code)) = UPPER(TRIM(:code))
            """)
    boolean existsByNormalizedCode(String code);

    @Query("""
            SELECT r
            FROM Room r
            WHERE UPPER(TRIM(r.code)) = UPPER(TRIM(:code))
            """)
    Optional<Room> findByNormalizedCode(String code);
}
