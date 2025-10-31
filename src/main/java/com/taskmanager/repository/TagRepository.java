package com.taskmanager.repository;

import com.taskmanager.entity.Tag;
import com.taskmanager.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TagRepository extends JpaRepository<Tag, UUID> {
    List<Tag> findByUserOrderByCreatedAtDesc(User user);
    Optional<Tag> findByIdAndUser(UUID id, User user);
    Optional<Tag> findByUserAndName(User user, String name);
}