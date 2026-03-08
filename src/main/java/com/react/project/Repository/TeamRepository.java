package com.react.project.Repository;

import com.react.project.Model.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {
    List<Team> findByProjectId(Long projectId);
    List<Team> findByTeamLeaderId(Long leaderId);

    @Modifying
    @Transactional
    @Query("UPDATE Team t SET t.teamLeader = null WHERE t.teamLeader.id = :userId")
    void nullifyTeamLeader(@Param("userId") Long userId);
}
