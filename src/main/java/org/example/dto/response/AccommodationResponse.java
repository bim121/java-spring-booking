package org.example.dto.response;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.model.AccommodationType;
import org.example.model.Address;

@Data
@AllArgsConstructor
public class AccommodationResponse {
    private Long id;
    private AccommodationType type;
    private Address location;
    private String size;
    private List<String> amenities;
    private BigDecimal dailyRate;
    private Integer availability;
}
