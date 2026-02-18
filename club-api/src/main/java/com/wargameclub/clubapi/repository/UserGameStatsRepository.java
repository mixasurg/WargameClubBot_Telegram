package com.wargameclub.clubapi.repository;

import com.wargameclub.clubapi.entity.UserGameStats;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserGameStatsRepository extends JpaRepository<UserGameStats, Long> {
}
