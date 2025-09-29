package com.backoffice.fitandflex.repository;

import com.backoffice.fitandflex.entity.Reservation;
import com.backoffice.fitandflex.entity.User;
import com.backoffice.fitandflex.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByUser(User user);
    List<Reservation> findBySchedule(Schedule schedule);
}
