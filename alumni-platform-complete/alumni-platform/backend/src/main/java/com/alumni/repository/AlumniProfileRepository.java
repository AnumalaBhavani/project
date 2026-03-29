package com.alumni.repository;

import com.alumni.entity.AlumniProfile;
import com.alumni.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AlumniProfileRepository extends JpaRepository<AlumniProfile, Long> {

    Optional<AlumniProfile> findByUser(User user);
    Optional<AlumniProfile> findByUserId(Long userId);

    @Query("""
        SELECT a FROM AlumniProfile a
        WHERE (:company IS NULL OR LOWER(a.currentCompany) LIKE LOWER(CONCAT('%', :company, '%')))
        AND (:domain IS NULL OR LOWER(a.domain) LIKE LOWER(CONCAT('%', :domain, '%')))
        AND (:location IS NULL OR LOWER(a.location) LIKE LOWER(CONCAT('%', :location, '%')))
        AND (:graduationYear IS NULL OR a.graduationYear = :graduationYear)
        AND (:mentorship IS NULL OR a.availableForMentorship = :mentorship)
        AND a.user.active = true AND a.user.verified = true
    """)
    List<AlumniProfile> searchAlumni(
            @Param("company") String company,
            @Param("domain") String domain,
            @Param("location") String location,
            @Param("graduationYear") Integer graduationYear,
            @Param("mentorship") Boolean mentorship
    );

    @Query("SELECT a FROM AlumniProfile a WHERE a.lastVerifiedAt IS NULL OR a.lastVerifiedAt < :cutoff")
    List<AlumniProfile> findAlumniNeedingVerification(@Param("cutoff") LocalDateTime cutoff);

    @Query("SELECT AVG(a.profileCompleteness) FROM AlumniProfile a WHERE a.user.active = true")
    Double findAverageProfileCompleteness();

    @Query("SELECT a FROM AlumniProfile a WHERE a.profileCompleteness < :threshold AND a.user.active = true")
    List<AlumniProfile> findIncompleteProfiles(@Param("threshold") int threshold);

    @Query("""
        SELECT a FROM AlumniProfile a
        WHERE a.user.active = false AND a.user.verified = false
        ORDER BY a.createdAt DESC
    """)
    List<AlumniProfile> findPendingApprovals();
}
