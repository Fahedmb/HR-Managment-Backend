package com.react.project.Repository;

import com.react.project.Model.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MeetingRepository extends JpaRepository<Meeting, Long> {
    List<Meeting> findByOrganizerId(Long organizerId);

    @Query("SELECT m FROM Meeting m JOIN m.attendees a WHERE a.id = :userId")
    List<Meeting> findByAttendeeId(Long userId);

    @Query("SELECT m FROM Meeting m WHERE m.organizer.id = :userId OR :userId IN (SELECT a.id FROM m.attendees a)")
    List<Meeting> findAllForUser(Long userId);

    List<Meeting> findByStartTimeBetween(LocalDateTime from, LocalDateTime to);

    @Modifying
    @Transactional
    @Query("UPDATE Meeting m SET m.organizer = null WHERE m.organizer.id = :userId")
    void nullifyOrganizer(@Param("userId") Long userId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM meeting_attendees WHERE user_id = :userId", nativeQuery = true)
    void removeFromAttendees(@Param("userId") Long userId);
}
