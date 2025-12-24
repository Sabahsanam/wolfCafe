package edu.ncsu.csc326.wolfcafe.service;

import java.util.List;

import edu.ncsu.csc326.wolfcafe.dto.OrderDto;
import edu.ncsu.csc326.wolfcafe.entity.OrderStatus;

/**
 * Service for Orders
 */
public interface OrderService {
    /**
     * Gets all the orders
     *
     * @return a list of orders
     */
    List<OrderDto> getAllOrders ();

    /**
     * Gets an order by the id
     *
     * @param id
     *            id of the order
     * @return OrderDto of the order
     */
    OrderDto getOrderById ( Long id );

    /**
     * Gets an order by the name
     *
     * @param name
     *            name of the order
     * @return OrderDto of the order
     */
    List<OrderDto> getOrderByName ( String name );

    /**
     * Creates an order
     *
     * @param orderDto
     *            of the order to create
     * @return OrderDto of the created order
     */
    OrderDto createOrder ( OrderDto orderDto );

    /**
     * Updates an order
     *
     * @param id
     *            id of the order to update
     * @param orderDto
     *            new changes for the order
     * @return order after changes have been made
     */
    OrderDto updateOrder ( Long id, OrderDto orderDto );

    /**
     * Deletes an order
     *
     * @param id
     *            id of order to delete
     */
    void deleteOrder ( Long id );

    /**
     * Updates the status of an order
     *
     * @param id
     *            the id of the order to update
     * @param status
     *            the new status of the order
     * @param role
     *            the role of the user updating the status
     * @param username
     *            the name of the user updating the status
     * @return the order with the new status
     */
    OrderDto updateStatus ( Long id, OrderStatus status, String role, String username );

    /**
     * Gets tax rate
     *
     * @return tax
     */
    double getTax ();

    /**
     * Sets tax rate
     *
     * @param rate
     *            tax to set
     */
    void setTax ( double rate );

}
