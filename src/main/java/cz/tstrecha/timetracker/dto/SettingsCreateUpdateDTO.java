package cz.tstrecha.timetracker.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SettingsCreateUpdateDTO {
    private long id;

    private LocalDate validFrom;

    private LocalDate validTo;

    @Positive
    private BigDecimal moneyPerHour;

    @Positive
    private BigDecimal moneyPerMonth;

    @NotNull
    private String name;

    private String note;
}
