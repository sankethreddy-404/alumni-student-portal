package com.alumniportal.service;

import com.alumniportal.dto.MaterialResponse;
import com.alumniportal.entity.Event;
import com.alumniportal.entity.Material;
import com.alumniportal.exception.ApiException;
import com.alumniportal.repository.EventRepository;
import com.alumniportal.repository.MaterialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MaterialService {

    private final MaterialRepository materialRepository;
    private final EventRepository eventRepository;
    private final FileStorageService fileStorageService;

    @Transactional
    public MaterialResponse upload(String title, String description, Long eventId, MultipartFile file) {
        Event event = null;
        if (eventId != null) {
            event = eventRepository.findById(eventId)
                    .orElseThrow(() -> new ApiException("Event not found", HttpStatus.NOT_FOUND));
        }
        String fileUrl = fileStorageService.store(file, "materials");
        Material material = Material.builder()
                .title(title)
                .description(description)
                .fileUrl(fileUrl)
                .event(event)
                .build();
        material = materialRepository.save(material);
        return toResponse(material);
    }

    public List<MaterialResponse> listAll() {
        return materialRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<MaterialResponse> listByEvent(Long eventId) {
        return materialRepository.findByEventId(eventId).stream().map(this::toResponse).collect(Collectors.toList());
    }

    private MaterialResponse toResponse(Material m) {
        return MaterialResponse.builder()
                .id(m.getId())
                .title(m.getTitle())
                .description(m.getDescription())
                .fileUrl(m.getFileUrl())
                .eventId(m.getEvent() != null ? m.getEvent().getId() : null)
                .uploadedAt(m.getUploadedAt())
                .build();
    }
}
