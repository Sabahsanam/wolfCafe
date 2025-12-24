package edu.ncsu.csc326.wolfcafe.dto;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Item for data transfer.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ItemDto {

    /** Item id */
    private Long    id;

    /** Item name */
    private String  name;

    /** Item description */
    private String  description;

    /** Item amount */
    @Min ( value = 0, message = "Item amount cannot be negative." )
    private Integer amount;

    /** Item price */
    private double  price;
}
