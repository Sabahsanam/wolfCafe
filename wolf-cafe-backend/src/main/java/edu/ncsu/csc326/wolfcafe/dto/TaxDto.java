package edu.ncsu.csc326.wolfcafe.dto;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Tax for setting tax rate
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TaxDto {

    /** Id */
    private Long   id;

    /** Tax Rate */
    @Min ( value = 0, message = "Tax rate cannot be negative." )
    private double rate;
}
