package org.example.service.accommodation.impl;

import java.util.List;
import lombok.AllArgsConstructor;
import org.example.dto.request.AccommodationRequest;
import org.example.dto.response.AccommodationResponse;
import org.example.entity.Accommodation;
import org.example.mapper.AccommodationMapper;
import org.example.repository.AccommodationRepository;
import org.example.service.accommodation.AccommodationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class AccommodationServiceImpl implements AccommodationService {
    private final AccommodationRepository accommodationRepository;
    private final AccommodationMapper accommodationMapper;

    @Transactional
    public AccommodationResponse createAccommodation(AccommodationRequest request) {
        Accommodation accommodation = accommodationMapper.toEntity(request);
        Accommodation saved = accommodationRepository.save(accommodation);
        return accommodationMapper.toResponse(saved);
    }

    public List<AccommodationResponse> getAllAccommodations() {
        List<Accommodation> accommodations = accommodationRepository.findAll();
        return accommodationMapper.toResponseList(accommodations);
    }

    public AccommodationResponse getAccommodationById(Long id) {
        Accommodation accommodation = accommodationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Accommodation not found"));
        return accommodationMapper.toResponse(accommodation);
    }

    @Transactional
    public AccommodationResponse updateAccommodation(Long id, AccommodationRequest request) {
        Accommodation accommodation = accommodationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Accommodation not found"));
        accommodationMapper.updateEntityFromRequest(request, accommodation);
        Accommodation updated = accommodationRepository.save(accommodation);
        return accommodationMapper.toResponse(updated);
    }

    @Transactional
    public void deleteAccommodation(Long id) {
        if (!accommodationRepository.existsById(id)) {
            throw new IllegalArgumentException("Accommodation not found");
        }
        accommodationRepository.deleteById(id);
    }
}
