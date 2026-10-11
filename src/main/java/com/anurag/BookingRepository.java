package com.anurag.backend;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    boolean existsByCourt_IdAndStatusAndEndTimeAfter(
        Long courtId,
        String status,
        LocalDateTime time
    );

    boolean existsByStudent_IdAndStatusAndEndTimeAfter(
        Long studentId,
        String status,
        LocalDateTime time
    );

    List<Booking> findAllByStudent_IdOrderByStartTimeDesc(Long studentId);

    List<Booking> findAllByStatusAndEndTimeAfter(
        String status,
        LocalDateTime time
    );

    @Modifying
    @Transactional
    @Query("""
        update Booking b
        set b.status = 'COMPLETED'
        where b.status = 'ACTIVE'
          and b.endTime <= :now
        """)
    int completeExpiredBookings(@Param("now") LocalDateTime now);
}