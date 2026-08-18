package com.alumniportal.service;

import com.alumniportal.dto.EventRequest;
import com.alumniportal.dto.EventResponse;
import com.alumniportal.entity.*;
import com.alumniportal.exception.ApiException;
import com.alumniportal.repository.EventRegistrationRepository;
import com.alumniportal.repository.EventRepository;
import com.alumniportal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final EventRegistrationRepository eventRegistrationRepository;
    private final UserRepository userRepository;
    private final ContributionService contributionService;

    @Transactional
    public EventResponse createEvent(Long creatorId, EventRequest request) {
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));
        Event event = Event.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .eventDate(request.getEventDate())
                .location(request.getLocation())
                .createdBy(creator)
                .build();
        event = eventRepository.save(event);
        return toResponse(event, null);
    }

    public List<EventResponse> listAll(Long currentUserId) {
        return eventRepository.findAll().stream()
                .map(e -> toResponse(e, currentUserId))
                .collect(Collectors.toList());
    }

    @Transactional
    public void register(Long eventId, Long userId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ApiException("Event not found", HttpStatus.NOT_FOUND));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));

        if (eventRegistrationRepository.findByEventIdAndUserId(eventId, userId).isPresent()) {
            throw new ApiException("Already registered for this event", HttpStatus.CONFLICT);
        }

        EventRegistration registration = EventRegistration.builder()
                .event(event)
                .user(user)
                .build();
        eventRegistrationRepository.save(registration);
    }

    @Transactional
    public void markAttended(Long eventId, Long userId) {
        EventRegistration registration = eventRegistrationRepository.findByEventIdAndUserId(eventId, userId)
                .orElseThrow(() -> new ApiException("Registration not found", HttpStatus.NOT_FOUND));
        registration.setAttended(true);
        eventRegistrationRepository.save(registration);

        if (registration.getUser().getRole() == Role.ALUMNI) {
            contributionService.recordContribution(userId, ContributionType.EVENT_ATTENDED, eventId);
        }
    }

    private EventResponse toResponse(Event event, Long currentUserId) {
        List<EventRegistration> regs = eventRegistrationRepository.findByEventId(event.getId());
        boolean registered = currentUserId != null &&
                regs.stream().anyMatch(r -> r.getUser().getId().equals(currentUserId));
        return EventResponse.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .eventDate(event.getEventDate())
                .location(event.getLocation())
                .createdByName(event.getCreatedBy() != null ? event.getCreatedBy().getName() : null)
                .registrationCount(regs.size())
                .registeredByCurrentUser(registered)
                .build();
    }
}
