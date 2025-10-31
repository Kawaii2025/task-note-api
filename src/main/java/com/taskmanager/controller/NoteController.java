package com.taskmanager.controller;

import com.taskmanager.dto.ApiResponse;
import com.taskmanager.dto.NoteDTO;
import com.taskmanager.entity.Note;
import com.taskmanager.service.NoteService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/notes")
public class NoteController {

    @Autowired
    private NoteService noteService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Note>>> getAllNotes(
            @RequestParam(required = false) String search) {
        try {
            List<Note> notes;

            if (search != null && !search.isEmpty()) {
                notes = noteService.searchNotes(search);
            } else {
                notes = noteService.getAllNotes();
            }

            return ResponseEntity.ok(ApiResponse.success(notes));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Note>> getNoteById(@PathVariable UUID id) {
        try {
            Note note = noteService.getNoteById(id);
            return ResponseEntity.ok(ApiResponse.success(note));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Note>> createNote(
            @Valid @RequestBody NoteDTO.CreateNoteRequest request) {
        try {
            Note note = noteService.createNote(request);
            return ResponseEntity.ok(ApiResponse.success("Note created successfully", note));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Note>> updateNote(
            @PathVariable UUID id,
            @Valid @RequestBody NoteDTO.UpdateNoteRequest request) {
        try {
            Note note = noteService.updateNote(id, request);
            return ResponseEntity.ok(ApiResponse.success("Note updated successfully", note));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PatchMapping("/{id}/pin")
    public ResponseEntity<ApiResponse<Note>> toggleNotePinned(@PathVariable UUID id) {
        try {
            Note note = noteService.toggleNotePinned(id);
            return ResponseEntity.ok(ApiResponse.success("Note pin status updated", note));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteNote(@PathVariable UUID id) {
        try {
            noteService.deleteNote(id);
            return ResponseEntity.ok(ApiResponse.success("Note deleted successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}