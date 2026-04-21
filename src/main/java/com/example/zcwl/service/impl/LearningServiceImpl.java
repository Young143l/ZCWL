package com.example.zcwl.service.impl;

import com.example.zcwl.entity.LearningRecord;
import com.example.zcwl.entity.UserLearningStats;
import com.example.zcwl.repository.LearningRecordRepository;
import com.example.zcwl.repository.UserLearningStatsRepository;
import com.example.zcwl.service.LearningService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class LearningServiceImpl implements LearningService {

    private static final Logger logger = LoggerFactory.getLogger(LearningServiceImpl.class);
    private final LearningRecordRepository learningRecordRepository;
    private final UserLearningStatsRepository userLearningStatsRepository;

    @Autowired
    public LearningServiceImpl(
            LearningRecordRepository learningRecordRepository,
            UserLearningStatsRepository userLearningStatsRepository) {
        this.learningRecordRepository = learningRecordRepository;
        this.userLearningStatsRepository = userLearningStatsRepository;
    }

    private Optional<LearningRecord> findRecord(String userId, Integer docId, Integer chapterId) {
        return learningRecordRepository.findByUserIdAndDocIdAndChapterIdNullable(userId, docId, chapterId);
    }

    @Override
    @Transactional
    public LearningRecord startLearning(String userId, Integer docId, Integer chapterId) {
        logger.info("Starting learning for user: {}, doc: {}, chapter: {}", userId, docId, chapterId);
        
        Optional<LearningRecord> existingRecord = findRecord(userId, docId, chapterId);

        LearningRecord record;
        if (existingRecord.isPresent()) {
            record = existingRecord.get();
            record.setLastAccessTime(LocalDateTime.now());
            record.setUpdatedAt(LocalDateTime.now());
        } else {
            record = new LearningRecord();
            record.setUserId(userId);
            record.setDocId(docId);
            record.setChapterId(chapterId);
            record.setStartTime(LocalDateTime.now());
            record.setLastAccessTime(LocalDateTime.now());
        }

        updateUserStatsOnStart(userId);

        return learningRecordRepository.save(record);
    }

    @Override
    @Transactional
    public LearningRecord updateProgress(String userId, Integer docId, Integer chapterId, Integer progress, Integer position) {
        logger.debug("Updating progress for user: {}, doc: {}, chapter: {}, progress: {}", userId, docId, chapterId, progress);
        
        Optional<LearningRecord> existingRecord = findRecord(userId, docId, chapterId);

        if (existingRecord.isPresent()) {
            LearningRecord record = existingRecord.get();
            boolean statusChanged = false;
            if (progress != null && progress >= record.getProgress()) {
                record.setProgress(progress);
                if (progress >= 100 && !"completed".equals(record.getStatus())) {
                    record.setStatus("completed");
                    record.setCompleteTime(LocalDateTime.now());
                    statusChanged = true;
                }
            }
            if (position != null) {
                record.setLastPosition(position);
            }
            record.setLastAccessTime(LocalDateTime.now());
            record.setUpdatedAt(LocalDateTime.now());
            LearningRecord savedRecord = learningRecordRepository.save(record);
            
            // 如果状态变为已完成，更新统计数据
            if (statusChanged) {
                updateUserStatsOnStart(userId);
            }
            return savedRecord;
        }

        return null;
    }

    @Override
    @Transactional
    public void endLearning(String userId, Integer docId, Integer chapterId) {
        logger.info("Ending learning for user: {}, doc: {}, chapter: {}", userId, docId, chapterId);
        
        updateUserStatsOnEnd(userId);
    }

    @Override
    public Optional<LearningRecord> getLearningRecord(String userId, Integer docId, Integer chapterId) {
        return findRecord(userId, docId, chapterId);
    }

    @Override
    public List<LearningRecord> getRecentLearning(String userId) {
        List<LearningRecord> records = learningRecordRepository.findByUserIdOrderByLastAccessTimeDesc(userId);
        return deduplicateRecords(records);
    }

    // 去重函数：按 docId + chapterId 分组，只保留最新的记录
    private List<LearningRecord> deduplicateRecords(List<LearningRecord> records) {
        Map<String, LearningRecord> uniqueMap = new HashMap<>();
        
        for (LearningRecord record : records) {
            String key = record.getDocId() + "-" + record.getChapterId();
            LearningRecord existing = uniqueMap.get(key);
            
            if (existing == null || record.getLastAccessTime().isAfter(existing.getLastAccessTime())) {
                uniqueMap.put(key, record);
            }
        }
        
        List<LearningRecord> result = new ArrayList<>(uniqueMap.values());
        result.sort(Comparator.comparing(LearningRecord::getLastAccessTime).reversed());
        return result;
    }

    @Override
    public UserLearningStats getUserStats(String userId) {
        return userLearningStatsRepository.findById(userId)
                .orElseGet(() -> {
                    UserLearningStats stats = new UserLearningStats();
                    stats.setUserId(userId);
                    return stats;
                });
    }

    @Override
    public List<LearningRecord> getDocChapterProgress(String userId, Integer docId) {
        return learningRecordRepository.findByUserIdAndDocIdOrderByChapterIdAsc(userId, docId);
    }

    @Override
    public List<Map<String, Object>> getAllDocProgress(String userId) {
        List<LearningRecord> records = learningRecordRepository.findByUserId(userId);
        
        Map<Integer, List<LearningRecord>> docGroups = new HashMap<>();
        for (LearningRecord record : records) {
            docGroups.computeIfAbsent(record.getDocId(), k -> new ArrayList<>()).add(record);
        }
        
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<Integer, List<LearningRecord>> entry : docGroups.entrySet()) {
            Integer docId = entry.getKey();
            List<LearningRecord> docRecords = entry.getValue();
            
            int totalChapters = docRecords.size();
            long completedChapters = docRecords.stream()
                    .filter(record -> "completed".equals(record.getStatus()))
                    .count();
            
            Map<String, Object> docProgress = new HashMap<>();
            docProgress.put("docId", docId);
            docProgress.put("totalChapters", totalChapters);
            docProgress.put("completedChapters", completedChapters);
            docProgress.put("completionRate", totalChapters > 0 ? (double) completedChapters / totalChapters : 0.0);
            
            result.add(docProgress);
        }
        
        result.sort((a, b) -> {
            Double rateA = (Double) a.get("completionRate");
            Double rateB = (Double) b.get("completionRate");
            return rateB.compareTo(rateA);
        });
        
        return result;
    }

    private void updateUserStatsOnStart(String userId) {
        Optional<UserLearningStats> statsOpt = userLearningStatsRepository.findById(userId);
        UserLearningStats stats;
        
        if (statsOpt.isPresent()) {
            stats = statsOpt.get();
        } else {
            stats = new UserLearningStats();
            stats.setUserId(userId);
            try {
                stats = userLearningStatsRepository.save(stats);
            } catch (DataIntegrityViolationException e) {
                logger.debug("UserLearningStats already exists for user: {}, fetching existing record", userId);
                stats = userLearningStatsRepository.findById(userId).orElseGet(() -> {
                    UserLearningStats newStats = new UserLearningStats();
                    newStats.setUserId(userId);
                    return newStats;
                });
            }
        }

        LocalDate today = LocalDate.now();
        LocalDate lastStudyDate = stats.getLastStudyDate();

        if (lastStudyDate == null) {
            stats.setStreakDays(1);
            stats.setTotalStudyDays(1);
        } else if (lastStudyDate.equals(today)) {
            // 今天已经学习过，不需要更新
        } else if (lastStudyDate.plusDays(1).equals(today)) {
            stats.setStreakDays(stats.getStreakDays() + 1);
            stats.setTotalStudyDays(stats.getTotalStudyDays() + 1);
        } else {
            stats.setStreakDays(1);
            stats.setTotalStudyDays(stats.getTotalStudyDays() + 1);
        }

        stats.setLastStudyDate(today);
        
        long completedCount = learningRecordRepository.countByUserIdAndStatus(userId, "completed");
        stats.setCompletedChapters((int) completedCount);
        
        // 计算学习文档数（唯一文档ID的数量）
        List<LearningRecord> records = learningRecordRepository.findByUserId(userId);
        Set<Integer> uniqueDocs = new HashSet<>();
        for (LearningRecord record : records) {
            uniqueDocs.add(record.getDocId());
        }
        stats.setTotalDocs(uniqueDocs.size());

        userLearningStatsRepository.save(stats);
    }

    private void updateUserStatsOnEnd(String userId) {
        Optional<UserLearningStats> statsOpt = userLearningStatsRepository.findById(userId);
        UserLearningStats stats;
        
        if (statsOpt.isPresent()) {
            stats = statsOpt.get();
        } else {
            stats = new UserLearningStats();
            stats.setUserId(userId);
            try {
                stats = userLearningStatsRepository.save(stats);
            } catch (DataIntegrityViolationException e) {
                logger.debug("UserLearningStats already exists for user: {}, fetching existing record", userId);
                stats = userLearningStatsRepository.findById(userId).orElseGet(() -> {
                    UserLearningStats newStats = new UserLearningStats();
                    newStats.setUserId(userId);
                    return newStats;
                });
            }
        }

        userLearningStatsRepository.save(stats);
    }
}
