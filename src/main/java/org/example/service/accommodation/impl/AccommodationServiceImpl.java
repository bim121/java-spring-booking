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

    @Override
    @Transactional
    public AccommodationResponse createAccommodation(AccommodationRequest request) {
        Accommodation accommodation = accommodationMapper.toEntity(request);
        Accommodation saved = accommodationRepository.save(accommodation);
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
        if (!accommodationRepository.existsById(id)) {
            throw new IllegalArgumentException("Accommodation not found");
        }
        accommodationRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Accommodation getAccommodationEntityById(Long id) {
        return accommodationRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Accommodation not found"));
    }
}
