package com.example.zcwl.repository;

import com.example.zcwl.entity.UserLearningStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserLearningStatsRepository extends JpaRepository<UserLearningStats, String> {
}
