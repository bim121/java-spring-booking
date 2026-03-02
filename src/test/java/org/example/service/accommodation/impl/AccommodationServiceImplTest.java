package org.example.service.accommodation.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.example.dto.request.AccommodationRequest;
import org.example.dto.response.AccommodationResponse;
import org.example.entity.Accommodation;
import org.example.mapper.AccommodationMapper;
import org.example.repository.AccommodationRepository;
import org.example.service.notification.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccommodationServiceImplTest {
    @Mock
    private AccommodationRepository accommodationRepository;
    @Mock
    private AccommodationMapper accommodationMapper;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private AccommodationServiceImpl accommodationService;

    @Test
    void createAccommodation_sendsNotification_evenIfNotificationFails() {
        AccommodationRequest request = new AccommodationRequest();

        Accommodation entity = new Accommodation();
        when(accommodationMapper.toEntity(request)).thenReturn(entity);

        Accommodation saved = new Accommodation();
        saved.setId(1L);
        when(accommodationRepository.save(entity)).thenReturn(saved);

        doThrow(new RuntimeException("fail")).when(notificationService)
                .notifyAccommodationCreated(saved);

        AccommodationResponse response = new AccommodationResponse(
                null, null, null, null, null, null, null);
        when(accommodationMapper.toResponse(saved)).thenReturn(response);

        accommodationService.createAccommodation(request);
        verify(accommodationRepository).save(entity);
        verify(notificationService).notifyAccommodationCreated(saved);
    }

    @Test
    void getAllAccommodations_mapsList() {
        when(accommodationRepository.findAll()).thenReturn(List.of(new Accommodation()));
        when(accommodationMapper.toResponseList(any()))
                .thenReturn(List.of(new AccommodationResponse(
                        null, null, null, null, null, null, null)));

        List<AccommodationResponse> result = accommodationService.getAllAccommodations();
        assertEquals(1, result.size());
    }

    @Test
    void getAccommodationEntityById_notFound_throws() {
        when(accommodationRepository.findById(99L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> accommodationService.getAccommodationEntityById(99L));
        assertEquals("Accommodation not found", ex.getMessage());
    }
}

