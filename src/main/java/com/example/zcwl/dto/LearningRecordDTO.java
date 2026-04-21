package com.example.zcwl.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LearningRecordDTO {
    private Integer docId;
    private Integer chapterId;
    private Integer progress;
    private Integer lastPosition;
    private Long timeSpent;
}
