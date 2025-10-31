package com.taskmanager.repository;

import com.taskmanager.entity.Note;
import com.taskmanager.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NoteRepository extends JpaRepository<Note, UUID> {
    List<Note> findByUserOrderByIsPinnedDescCreatedAtDesc(User user);

    @Query("SELECT n FROM Note n WHERE n.user = :user AND " +
            "(LOWER(n.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(n.content) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<Note> searchNotes(@Param("user") User user, @Param("search") String search);

    Optional<Note> findByIdAndUser(UUID id, User user);

    Long countByUser(User user);
    Long countByUserAndIsPinned(User user, Boolean isPinned);
}