package com.taskmanager.service;

import com.taskmanager.entity.User;
import com.taskmanager.repository.NoteRepository;
import com.taskmanager.repository.TaskRepository;
import com.taskmanager.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class StatsService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private UserRepository userRepository;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public Map<String, Object> getStatistics() {
        User user = getCurrentUser();

        Map<String, Object> stats = new HashMap<>();

        // Task statistics
        Map<String, Object> taskStats = new HashMap<>();
        Long totalTasks = taskRepository.countByUserAndCompleted(user, false) +
                taskRepository.countByUserAndCompleted(user, true);
        Long completedTasks = taskRepository.countByUserAndCompleted(user, true);
        Long activeTasks = taskRepository.countByUserAndCompleted(user, false);

        taskStats.put("total", totalTasks);
        taskStats.put("completed", completedTasks);
        taskStats.put("active", activeTasks);

        if (totalTasks > 0) {
            taskStats.put("completionRate", (completedTasks * 100.0) / totalTasks);
        } else {
            taskStats.put("completionRate", 0.0);
        }

        stats.put("tasks", taskStats);

        // Note statistics
        Map<String, Object> noteStats = new HashMap<>();
        Long totalNotes = noteRepository.countByUser(user);
        Long pinnedNotes = noteRepository.countByUserAndIsPinned(user, true);

        noteStats.put("total", totalNotes);
        noteStats.put("pinned", pinnedNotes);

        stats.put("notes", noteStats);

        return stats;
    }
}