package edu.ncsu.csc326.wolfcafe.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.ncsu.csc326.wolfcafe.dto.OrderDto;
import edu.ncsu.csc326.wolfcafe.dto.TaxDto;
import edu.ncsu.csc326.wolfcafe.entity.OrderLine;
import edu.ncsu.csc326.wolfcafe.entity.OrderStatus;
import edu.ncsu.csc326.wolfcafe.service.ItemService;
import edu.ncsu.csc326.wolfcafe.service.OrderService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

/**
 * REST controller for orders
 */
@RestController
@RequestMapping ( "/api/orders" )
@AllArgsConstructor
@CrossOrigin ( "*" )
public class OrderController {

    /** Service for the orders */
    private final OrderService orderService;

    /** Service for items */
    private final ItemService  itemService;

    /**
     * Gets all of the orders
     *
     * @return response of all the orders
     */
    @PreAuthorize ( "hasAnyRole('ADMIN', 'STAFF', 'CUSTOMER')" )
    @GetMapping
    public ResponseEntity<List<OrderDto>> getAllOrders () {
        return ResponseEntity.ok( orderService.getAllOrders() );
    }

    /**
     * Gets an order by id
     *
     * @param id
     *            id of the order
     * @return requested order
     */
    @PreAuthorize ( "hasAnyRole('ADMIN', 'STAFF', 'CUSTOMER')" )
    @GetMapping ( "/id/{id}" )
    public ResponseEntity<OrderDto> getOrderById ( @PathVariable ( "id" ) final Long id ) {
        return ResponseEntity.ok( orderService.getOrderById( id ) );
    }

    /**
     * Gets an order by name
     *
     * @param name
     *            name of the order
     * @return requested order
     */
    @PreAuthorize ( "hasAnyRole('ADMIN', 'STAFF', 'CUSTOMER')" )
    @GetMapping ( "/name/{name}" )
    public ResponseEntity<List<OrderDto>> getOrderByName ( @PathVariable final String name ) {
        return ResponseEntity.ok( orderService.getOrderByName( name ) );
    }

    /**
     * Creates an order
     *
     * @param orderDto
     *            details of the order
     * @return created order
     */
    @PreAuthorize ( "hasAnyRole('ADMIN', 'STAFF', 'CUSTOMER')" )
    @PostMapping
    public ResponseEntity<OrderDto> createOrder ( @RequestBody final OrderDto orderDto ) {
        final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        final String username = auth.getName();

        for ( final OrderLine line : orderDto.getOrderLines() ) {

            // Checks if item id exists
            if ( line.getItemId() == null ) {
                return new ResponseEntity<>( HttpStatus.BAD_REQUEST );
            }

            // Checks if item exists
            final var item = itemService.getItem( line.getItemId() );
            if ( item == null ) {
                return new ResponseEntity<>( HttpStatus.BAD_REQUEST );
            }

            // Checks the amount of item is greater than 0
            if ( line.getAmount() <= 0 ) {
                return new ResponseEntity<>( HttpStatus.BAD_REQUEST );
            }

        }

        orderDto.setName( username );
        return ResponseEntity.ok( orderService.createOrder( orderDto ) );
    }

    /**
     * Updates an order
     *
     * @param id
     *            id of order
     * @param orderDto
     *            changes to order
     * @return updated order
     */
    @PreAuthorize ( "hasAnyRole('ADMIN', 'STAFF')" )
    @PutMapping ( "/{id}" )
    public ResponseEntity<OrderDto> updateOrder ( @PathVariable ( "id" ) final Long id,
            @RequestBody final OrderDto orderDto ) {
        return ResponseEntity.ok( orderService.updateOrder( id, orderDto ) );
    }

    /**
     * Deletes an order
     *
     * @param id
     *            id of order to delete
     * @return success or failure
     */
    @PreAuthorize ( "hasAnyRole('ADMIN', 'STAFF')" )
    @DeleteMapping ( "/{id}" )
    public ResponseEntity<Void> deleteOrder ( @PathVariable ( "id" ) final Long id ) {
        orderService.deleteOrder( id );
        return ResponseEntity.ok().build();
    }

    /**
     * fulfills and order
     *
     * @param id
     *            id of the order
     * @param status
     *            the new status of the order
     * @param auth
     *            authorization of the user
     * @return the updated order
     */
    @PutMapping ( "/{id}/status" )
    @PreAuthorize ( "hasAnyRole('ADMIN', 'STAFF', 'CUSTOMER')" )
    public ResponseEntity< ? > updateOrderStatus ( @PathVariable ( "id" ) final Long id,
            @RequestBody final OrderStatus status, final Authentication auth ) {
        final String username = auth.getName();
        final String role = auth.getAuthorities().iterator().next().getAuthority();

        try {
            return ResponseEntity.ok( orderService.updateStatus( id, status, role, username ) );
        }
        catch ( final IllegalStateException e ) {
            return ResponseEntity.status( HttpStatus.BAD_REQUEST ).body( e.getMessage() );
        }

    }

    /**
     * Gets the orders by user
     *
     * @param username
     *            username of the user
     * @return a list of orders for the user
     */
    @GetMapping ( "/user/{username}" )
    public ResponseEntity<List<OrderDto>> getOrdersByUser ( @PathVariable final String username ) {
        return ResponseEntity.ok( orderService.getOrderByName( username ) );
    }

    /**
     * Sets tax rate
     *
     * @return tax
     *
     */
    @PreAuthorize ( "hasAnyRole('ADMIN', 'STAFF', 'CUSTOMER')" )
    @GetMapping ( "/tax" )
    public ResponseEntity<TaxDto> getTax () {
        final double rate = orderService.getTax();
        final TaxDto dto = new TaxDto();
        dto.setRate( rate );
        return ResponseEntity.ok( dto );
    }

    /**
     * Sets tax rate
     *
     * @param taxDto
     *            tax to set
     *
     */
    @PutMapping ( "/tax" )
    @PreAuthorize ( "hasRole('ADMIN')" )
    public void setTax ( @Valid @RequestBody final TaxDto taxDto ) {
        orderService.setTax( taxDto.getRate() );
    }

}
