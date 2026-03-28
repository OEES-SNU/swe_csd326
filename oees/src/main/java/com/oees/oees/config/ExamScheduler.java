package com.oees.oees.config;

import com.oees.oees.service.ExamService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExamScheduler {

    private final ExamService examService;

    // Runs every minute to activate/expire exams based on time windows
    @Scheduled(fixedDelay = 60000)
    public void syncExamStatuses() {
        examService.updateExamStatuses();
    }
}
