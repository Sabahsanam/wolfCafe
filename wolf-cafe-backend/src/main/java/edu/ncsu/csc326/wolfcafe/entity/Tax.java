package edu.ncsu.csc326.wolfcafe.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The tax for Orders
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Tax {

    /** Id */
    @Id
    @GeneratedValue
    private Long   id;

    /** Tax Rate */
    @Min ( value = 0, message = "Tax rate cannot be negative." )
    private double rate;
}
