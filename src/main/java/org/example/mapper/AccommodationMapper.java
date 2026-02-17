package org.example.mapper;

import java.util.List;
import org.example.dto.request.AccommodationRequest;
import org.example.dto.response.AccommodationResponse;
import org.example.entity.Accommodation;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AccommodationMapper {
    AccommodationResponse toResponse(Accommodation accommodation);

    List<AccommodationResponse> toResponseList(List<Accommodation> accommodations);

    Accommodation toEntity(AccommodationRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(AccommodationRequest request,
                                 @MappingTarget Accommodation accommodation);
}
