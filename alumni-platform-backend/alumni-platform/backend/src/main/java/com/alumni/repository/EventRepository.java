package com.alumni.repository;

import com.alumni.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByStatusOrderByEventDateAsc(Event.EventStatus status);
    List<Event> findByCreatedByIdOrderByEventDateDesc(Long userId);
}
