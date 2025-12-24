package edu.ncsu.csc326.wolfcafe.entity;

/**
 * Enum for the different states of an order
 */
public enum OrderStatus {
    /** Pending order */
    PENDING,
    /** The order has been fulfilled by the staff */
    FULFILLED,
    /** The order has been picked up by the customer */
    PICKED_UP
}
