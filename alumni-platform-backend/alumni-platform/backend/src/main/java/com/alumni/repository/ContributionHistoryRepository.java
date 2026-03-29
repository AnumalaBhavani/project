package com.alumni.repository;

import com.alumni.entity.ContributionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ContributionHistoryRepository extends JpaRepository<ContributionHistory, Long> {
    List<ContributionHistory> findByAlumniIdOrderByCreatedAtDesc(Long alumniId);

    @Query("SELECT c.alumni.id, SUM(c.points) as totalPoints FROM ContributionHistory c GROUP BY c.alumni.id ORDER BY totalPoints DESC")
    List<Object[]> findTopContributors();

    @Query("SELECT SUM(c.points) FROM ContributionHistory c WHERE c.alumni.id = :alumniId")
    Long sumPointsByAlumniId(Long alumniId);
}
