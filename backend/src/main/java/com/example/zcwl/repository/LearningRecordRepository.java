package com.example.zcwl.repository;

import com.example.zcwl.entity.LearningRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LearningRecordRepository extends JpaRepository<LearningRecord, Long> {
    
    List<LearningRecord> findByUserId(String userId);
    
    List<LearningRecord> findByUserIdAndDocIdAndChapterIdIsNull(String userId, Integer docId);
    
    List<LearningRecord> findByUserIdAndDocIdAndChapterId(String userId, Integer docId, Integer chapterId);
    
    @Query("SELECT lr FROM LearningRecord lr WHERE lr.userId = :userId AND lr.docId = :docId AND ((:chapterId IS NULL AND lr.chapterId IS NULL) OR lr.chapterId = :chapterId) ORDER BY lr.lastAccessTime DESC")
    List<LearningRecord> findAllByUserIdAndDocIdAndChapterIdNullable(
        @Param("userId") String userId, 
        @Param("docId") Integer docId, 
        @Param("chapterId") Integer chapterId
    );
    
    default Optional<LearningRecord> findByUserIdAndDocIdAndChapterIdNullable(String userId, Integer docId, Integer chapterId) {
        List<LearningRecord> results = findAllByUserIdAndDocIdAndChapterIdNullable(userId, docId, chapterId);
        if (results.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(results.get(0));
    }

    @Query("SELECT lr FROM LearningRecord lr WHERE lr.userId = :userId ORDER BY lr.lastAccessTime DESC")
    List<LearningRecord> findByUserIdOrderByLastAccessTimeDesc(@Param("userId") String userId);

    @Query("SELECT lr FROM LearningRecord lr WHERE lr.userId = :userId AND lr.docId = :docId ORDER BY lr.chapterId ASC")
    List<LearningRecord> findByUserIdAndDocIdOrderByChapterIdAsc(@Param("userId") String userId, @Param("docId") Integer docId);
    
    long countByUserIdAndStatus(String userId, String status);

    @Query("SELECT COUNT(DISTINCT lr.docId) FROM LearningRecord lr WHERE lr.userId = :userId")
    long countDistinctDocIdByUserId(@Param("userId") String userId);
}
