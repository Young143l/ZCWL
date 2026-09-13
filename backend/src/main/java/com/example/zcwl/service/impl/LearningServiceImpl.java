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

    @Override
    public List<Map<String, Object>> getHeatmap(String userId) {
        // 获取用户所有学习记录
        List<LearningRecord> records = learningRecordRepository.findByUserId(userId);
        
        // 计算过去365天的时间范围
        LocalDate today = LocalDate.now();
        LocalDate oneYearAgo = today.minusDays(364);
        
        // 按日期统计学习活动次数（使用lastAccessTime的日期部分）
        Map<LocalDate, Integer> dateCountMap = new HashMap<>();
        for (LearningRecord record : records) {
            if (record.getLastAccessTime() != null) {
                LocalDate accessDate = record.getLastAccessTime().toLocalDate();
                // 只统计过去365天内的记录
                if (!accessDate.isBefore(oneYearAgo) && !accessDate.isAfter(today)) {
                    dateCountMap.merge(accessDate, 1, Integer::sum);
                }
            }
        }
        
        // 生成365天的数据，填充没有活动的日期
        List<Map<String, Object>> heatmapData = new ArrayList<>();
        for (int i = 364; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            int count = dateCountMap.getOrDefault(date, 0);
            int level = calculateLevel(count);
            
            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", date.toString()); // LocalDate.toString() returns "yyyy-MM-dd"
            dayData.put("count", count);
            dayData.put("level", level);
            
            heatmapData.add(dayData);
        }
        
        return heatmapData;
    }
    
    /**
     * 根据学习次数计算热力等级 (0-4)
     * 与前端组件中的颜色等级对应：
     * 0 = 无学习, 1 = 轻度学习, 2 = 中等学习, 3 = 较高学习, 4 = 活跃学习
     */
    private int calculateLevel(int count) {
        if (count <= 0) return 0;
        if (count == 1) return 1;
        if (count <= 3) return 2;
        if (count <= 6) return 3;
        return 4;
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
        
        // 计算学习文档数（使用数据库查询唯一文档ID的数量）
        long distinctDocCount = learningRecordRepository.countDistinctDocIdByUserId(userId);
        stats.setTotalDocs((int) distinctDocCount);

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
