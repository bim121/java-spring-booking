package org.example.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.example.model.AccommodationType;
import org.example.model.Address;

@Data
public class AccommodationRequest {
    @NotNull(message = "Type is required")
    private AccommodationType type;

    @NotNull(message = "Location is required")
    private Address location;

    @NotBlank(message = "Size is required")
    private String size;

    private List<String> amenities = new ArrayList<>();

    @NotNull(message = "Daily rate is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Daily rate must be greater than 0")
    private BigDecimal dailyRate;

    @NotNull(message = "Availability is required")
    @Min(value = 0, message = "Availability must be 0 or greater")
    private Integer availability;
}
