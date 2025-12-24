package edu.ncsu.csc326.wolfcafe.dto;

import java.util.List;

import edu.ncsu.csc326.wolfcafe.entity.OrderLine;
import edu.ncsu.csc326.wolfcafe.entity.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Dto for orders
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {
    /** id of the order */
    private Long            id;

    /** Name of the order */
    private String          name;

    /** Total price of the order */
    private double          totalPrice;

    /** Tip placed on the order */
    private double          tip;

    /** The tax rate at the time of the order */
    private double          taxrate;

    /** The status of the order */
    private OrderStatus     status;

    /** List of items in the order */
    private List<OrderLine> orderLines;
}
