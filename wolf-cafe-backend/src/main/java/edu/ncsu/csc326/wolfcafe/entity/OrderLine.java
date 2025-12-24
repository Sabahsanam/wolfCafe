package edu.ncsu.csc326.wolfcafe.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Stores info about an item in an order
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class OrderLine {
    /** Id of item being ordered */
    private Long   itemId;

    /** Amount of item ordered */
    private int    amount;

    /** Snapshot of price at time of order */
    private double price;

    /** Snapshot of item name at time of order */
    private String itemName;
}
