package com.taskmanager.service;

import com.taskmanager.dto.TagDTO;
import com.taskmanager.entity.Tag;
import com.taskmanager.entity.User;
import com.taskmanager.repository.TagRepository;
import com.taskmanager.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class TagService {

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private UserRepository userRepository;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public List<Tag> getAllTags() {
        User user = getCurrentUser();
        return tagRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public Tag getTagById(UUID id) {
        User user = getCurrentUser();
        return tagRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new RuntimeException("Tag not found"));
    }

    public Tag createTag(TagDTO.CreateTagRequest request) {
        User user = getCurrentUser();

        // Check if tag with same name exists
        if (tagRepository.findByUserAndName(user, request.getName()).isPresent()) {
            throw new RuntimeException("Tag with this name already exists");
        }

        Tag tag = new Tag();
        tag.setUser(user);
        tag.setName(request.getName());
        tag.setColor(request.getColor());

        return tagRepository.save(tag);
    }

    public Tag updateTag(UUID id, TagDTO.UpdateTagRequest request) {
        User user = getCurrentUser();
        Tag tag = tagRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new RuntimeException("Tag not found"));

        if (request.getName() != null) {
            // Check if new name conflicts with existing tag
            tagRepository.findByUserAndName(user, request.getName())
                    .ifPresent(existingTag -> {
                        if (!existingTag.getId().equals(id)) {
                            throw new RuntimeException("Tag with this name already exists");
                        }
                    });
            tag.setName(request.getName());
        }
        if (request.getColor() != null) {
            tag.setColor(request.getColor());
        }

        return tagRepository.save(tag);
    }

    public void deleteTag(UUID id) {
        User user = getCurrentUser();
        Tag tag = tagRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new RuntimeException("Tag not found"));

        tagRepository.delete(tag);
    }
}