package com.alumni.repository;

import com.alumni.entity.MentorshipRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MentorshipRequestRepository extends JpaRepository<MentorshipRequest, Long> {
    List<MentorshipRequest> findByStudentId(Long studentId);
    List<MentorshipRequest> findByAlumniId(Long alumniId);
    List<MentorshipRequest> findByAlumniIdAndStatus(Long alumniId, MentorshipRequest.RequestStatus status);
    long countByAlumniIdAndStatus(Long alumniId, MentorshipRequest.RequestStatus status);

    @Query("SELECT COUNT(m) FROM MentorshipRequest m WHERE m.status = 'COMPLETED'")
    long countCompletedSessions();
}
