package org.example.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.util.List;
import org.example.dto.request.AccommodationRequest;
import org.example.dto.response.AccommodationResponse;
import org.example.entity.Accommodation;
import org.example.model.AccommodationType;
import org.example.model.Address;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class AccommodationMapperTest {
    private final AccommodationMapper mapper = Mappers.getMapper(AccommodationMapper.class);

    @Test
    void toEntity_mapsFields() {
        AccommodationRequest request = new AccommodationRequest();
        request.setType(AccommodationType.APARTMENT);
        request.setSize("L");
        request.setAmenities(List.of("wifi"));
        request.setDailyRate(new BigDecimal("10.00"));
        request.setAvailability(3);

        Address address = new Address();
        address.setCity("Kyiv");
        request.setLocation(address);

        Accommodation entity = mapper.toEntity(request);
        assertEquals(AccommodationType.APARTMENT, entity.getType());
        assertEquals("L", entity.getSize());
        assertEquals(1, entity.getAmenities().size());
        assertEquals(new BigDecimal("10.00"), entity.getDailyRate());
        assertEquals(3, entity.getAvailability());
        assertNotNull(entity.getLocation());
    }

    @Test
    void toResponse_mapsFields() {
        Accommodation accommodation = new Accommodation();
        accommodation.setId(1L);
        accommodation.setType(AccommodationType.HOUSE);
        accommodation.setSize("S");
        accommodation.setAmenities(List.of("kitchen"));
        accommodation.setDailyRate(new BigDecimal("20.00"));
        accommodation.setAvailability(2);

        AccommodationResponse response = mapper.toResponse(accommodation);
        assertEquals(1L, response.getId());
        assertEquals(AccommodationType.HOUSE, response.getType());
        assertEquals("S", response.getSize());
        assertEquals(new BigDecimal("20.00"), response.getDailyRate());
        assertEquals(2, response.getAvailability());
    }
}

