package com.taskmanager.service;

import com.taskmanager.dto.NoteDTO;
import com.taskmanager.entity.Note;
import com.taskmanager.entity.Tag;
import com.taskmanager.entity.User;
import com.taskmanager.repository.NoteRepository;
import com.taskmanager.repository.TagRepository;
import com.taskmanager.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class NoteService {

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TagRepository tagRepository;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public List<Note> getAllNotes() {
        User user = getCurrentUser();
        return noteRepository.findByUserOrderByIsPinnedDescCreatedAtDesc(user);
    }

    public List<Note> searchNotes(String search) {
        User user = getCurrentUser();
        return noteRepository.searchNotes(user, search);
    }

    public Note getNoteById(UUID id) {
        User user = getCurrentUser();
        return noteRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new RuntimeException("Note not found"));
    }

    @Transactional
    public Note createNote(NoteDTO.CreateNoteRequest request) {
        User user = getCurrentUser();

        Note note = new Note();
        note.setUser(user);
        note.setTitle(request.getTitle());
        note.setContent(request.getContent());

        // Handle tags
        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            Set<Tag> tags = new HashSet<>();
            for (UUID tagId : request.getTagIds()) {
                Tag tag = tagRepository.findByIdAndUser(tagId, user)
                        .orElseThrow(() -> new RuntimeException("Tag not found: " + tagId));
                tags.add(tag);
            }
            note.setTags(tags);
        }

        return noteRepository.save(note);
    }

    @Transactional
    public Note updateNote(UUID id, NoteDTO.UpdateNoteRequest request) {
        User user = getCurrentUser();
        Note note = noteRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new RuntimeException("Note not found"));

        if (request.getTitle() != null) {
            note.setTitle(request.getTitle());
        }
        if (request.getContent() != null) {
            note.setContent(request.getContent());
        }
        if (request.getIsPinned() != null) {
            note.setIsPinned(request.getIsPinned());
        }

        // Update tags
        if (request.getTagIds() != null) {
            Set<Tag> tags = new HashSet<>();
            for (UUID tagId : request.getTagIds()) {
                Tag tag = tagRepository.findByIdAndUser(tagId, user)
                        .orElseThrow(() -> new RuntimeException("Tag not found: " + tagId));
                tags.add(tag);
            }
            note.setTags(tags);
        }

        return noteRepository.save(note);
    }

    @Transactional
    public Note toggleNotePinned(UUID id) {
        User user = getCurrentUser();
        Note note = noteRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new RuntimeException("Note not found"));

        note.setIsPinned(!note.getIsPinned());
        return noteRepository.save(note);
    }

    public void deleteNote(UUID id) {
        User user = getCurrentUser();
        Note note = noteRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new RuntimeException("Note not found"));

        noteRepository.delete(note);
    }
}