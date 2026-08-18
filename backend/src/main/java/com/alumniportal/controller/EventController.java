package com.alumniportal.controller;

import com.alumniportal.dto.EventRequest;
import com.alumniportal.dto.EventResponse;
import com.alumniportal.dto.MaterialResponse;
import com.alumniportal.dto.MessageResponse;
import com.alumniportal.security.CustomUserDetails;
import com.alumniportal.service.EventService;
import com.alumniportal.service.MaterialService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;
    private final MaterialService materialService;

    @GetMapping
    public ResponseEntity<List<EventResponse>> listAll(@AuthenticationPrincipal CustomUserDetails principal) {
        Long userId = principal != null ? principal.getId() : null;
        return ResponseEntity.ok(eventService.listAll(userId));
    }

    // Admins or alumni can create events
    @PostMapping
    public ResponseEntity<EventResponse> create(@AuthenticationPrincipal CustomUserDetails principal,
                                                 @RequestBody EventRequest request) {
        if (!"ADMIN".equals(principal.getRole()) && !"ALUMNI".equals(principal.getRole())) {
            throw new com.alumniportal.exception.ApiException(
                    "Only admins or alumni can create events", org.springframework.http.HttpStatus.FORBIDDEN);
        }
        return ResponseEntity.ok(eventService.createEvent(principal.getId(), request));
    }

    @PostMapping("/{eventId}/register")
    public ResponseEntity<MessageResponse> register(@AuthenticationPrincipal CustomUserDetails principal,
                                                      @PathVariable Long eventId) {
        eventService.register(eventId, principal.getId());
        return ResponseEntity.ok(new MessageResponse("Registered successfully"));
    }

    @PostMapping("/{eventId}/attended/{userId}")
    public ResponseEntity<MessageResponse> markAttended(@PathVariable Long eventId, @PathVariable Long userId) {
        eventService.markAttended(eventId, userId);
        return ResponseEntity.ok(new MessageResponse("Marked as attended"));
    }

    @GetMapping("/{eventId}/materials")
    public ResponseEntity<List<MaterialResponse>> materialsForEvent(@PathVariable Long eventId) {
        return ResponseEntity.ok(materialService.listByEvent(eventId));
    }

    @PostMapping(value = "/materials", consumes = "multipart/form-data")
    public ResponseEntity<MaterialResponse> uploadMaterial(@RequestParam String title,
                                                            @RequestParam(required = false) String description,
                                                            @RequestParam(required = false) Long eventId,
                                                            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(materialService.upload(title, description, eventId, file));
    }
}
