package com.alumni.repository;

import com.alumni.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {

    @Query("""
        SELECT j FROM Job j WHERE j.status = 'APPROVED' AND j.active = true
        AND (:domain IS NULL OR LOWER(j.domain) LIKE LOWER(CONCAT('%', :domain, '%')))
        AND (:location IS NULL OR LOWER(j.location) LIKE LOWER(CONCAT('%', :location, '%')))
        AND (:jobType IS NULL OR j.jobType = :jobType)
        ORDER BY j.createdAt DESC
    """)
    List<Job> searchApprovedJobs(
            @Param("domain") String domain,
            @Param("location") String location,
            @Param("jobType") Job.JobType jobType
    );

    List<Job> findByPostedById(Long alumniId);
    List<Job> findByStatus(Job.JobStatus status);
    long countByStatus(Job.JobStatus status);
    long countByPostedById(Long alumniId);
}
