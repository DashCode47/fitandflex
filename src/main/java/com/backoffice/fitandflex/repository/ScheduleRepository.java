package com.backoffice.fitandflex.repository;

import com.backoffice.fitandflex.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    List<Schedule> findByStartTimeBetween(LocalDateTime start, LocalDateTime end);
}
