package org.example.service.accommodation.impl;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.request.AccommodationRequest;
import org.example.dto.response.AccommodationResponse;
import org.example.entity.Accommodation;
import org.example.exception.EntityNotFoundException;
import org.example.mapper.AccommodationMapper;
import org.example.repository.AccommodationRepository;
import org.example.service.accommodation.AccommodationService;
import org.example.service.notification.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccommodationServiceImpl implements AccommodationService {
    private final AccommodationRepository accommodationRepository;
    private final AccommodationMapper accommodationMapper;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public AccommodationResponse createAccommodation(AccommodationRequest request) {
        Accommodation accommodation = accommodationMapper.toEntity(request);
        Accommodation saved = accommodationRepository.save(accommodation);
        try {
            notificationService.notifyAccommodationCreated(saved);
        } catch (Exception e) {
            log.error("Failed to send notification for accommodation creation", e);
        }
        return accommodationMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccommodationResponse> getAllAccommodations() {
        return accommodationMapper.toResponseList(
                accommodationRepository.findAll()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AccommodationResponse getAccommodationById(Long id) {
        return accommodationMapper.toResponse(
                getAccommodationEntityById(id)
        );
    }

    @Override
    @Transactional
    public AccommodationResponse updateAccommodation(
            Long id,
            AccommodationRequest request) {

        Accommodation accommodation = getAccommodationEntityById(id);

        accommodationMapper.updateEntityFromRequest(request, accommodation);

        Accommodation updated = accommodationRepository.save(accommodation);

        return accommodationMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteAccommodation(Long id) {
        Accommodation accommodation = getAccommodationEntityById(id);
        accommodationRepository.deleteById(id);
        try {
            notificationService.notifyAccommodationReleased(accommodation);
        } catch (Exception e) {
            log.error("Failed to send notification for accommodation release", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Accommodation getAccommodationEntityById(Long id) {
        return accommodationRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException("Accommodation not found with id: " + id));
    }
}
