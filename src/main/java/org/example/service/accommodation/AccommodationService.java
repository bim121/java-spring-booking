package org.example.service.accommodation;

import java.util.List;
import org.example.dto.request.AccommodationRequest;
import org.example.dto.response.AccommodationResponse;

public interface AccommodationService {
    AccommodationResponse createAccommodation(AccommodationRequest request);

    List<AccommodationResponse> getAllAccommodations();

    AccommodationResponse getAccommodationById(Long id);

    AccommodationResponse updateAccommodation(Long id, AccommodationRequest request);

    void deleteAccommodation(Long id);
}
