package edu.ncsu.csc326.wolfcafe.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import edu.ncsu.csc326.wolfcafe.dto.OrderDto;
import edu.ncsu.csc326.wolfcafe.entity.Item;
import edu.ncsu.csc326.wolfcafe.entity.Order;
import edu.ncsu.csc326.wolfcafe.entity.OrderLine;
import edu.ncsu.csc326.wolfcafe.entity.OrderStatus;
import edu.ncsu.csc326.wolfcafe.entity.Tax;
import edu.ncsu.csc326.wolfcafe.exception.ResourceNotFoundException;
import edu.ncsu.csc326.wolfcafe.repository.ItemRepository;
import edu.ncsu.csc326.wolfcafe.repository.OrderRepository;
import edu.ncsu.csc326.wolfcafe.repository.TaxRepository;
import edu.ncsu.csc326.wolfcafe.service.OrderService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

/**
 * Implementation of OrderService
 */
@Service
@AllArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    /** OrderRepository to be used */
    private final OrderRepository orderRepository;

    /** Repository for the items */
    private final ItemRepository  itemRepository;

    /** TaxRespository to be used */
    private final TaxRepository   taxRepository;

    /**
     * Converts normal order to dto
     *
     * @param order
     *            order to be converted
     * @return dto of order
     */
    private OrderDto mapToDto ( final Order order ) {
        final OrderDto dto = new OrderDto();
        dto.setId( order.getId() );
        dto.setName( order.getName() );
        dto.setTotalPrice( order.getTotalPrice() );
        dto.setOrderLines( order.getOrderLines() );
        dto.setStatus( order.getStatus() );
        dto.setTip( order.getTip() );
        dto.setTaxrate( order.getTaxrate() );
        return dto;
    }

    /**
     * Converts order dto to order entity
     *
     * @param orderDto
     *            dto to be converted
     * @return entity of the dto
     */
    private Order mapToEntity ( final OrderDto orderDto ) {
        final Order order = new Order();
        order.setId( orderDto.getId() );
        order.setName( orderDto.getName() );
        order.setTotalPrice( orderDto.getTotalPrice() );
        order.setOrderLines( orderDto.getOrderLines() );
        order.setTip( orderDto.getTip() );
        order.setStatus( orderDto.getStatus() );
        order.setTaxrate( orderDto.getTaxrate() );
        return order;
    }

    /**
     * Gets all the orders
     *
     * @return List of all the orders
     */
    @Override
    public List<OrderDto> getAllOrders () {
        return orderRepository.findAll().stream().map( this::mapToDto ).collect( Collectors.toList() );
    }

    /**
     * Gets an order by id
     *
     * @param id
     *            id of order to get
     * @return dto of the order
     */
    @Override
    public OrderDto getOrderById ( final Long id ) {
        final Order order = orderRepository.findById( id )
                .orElseThrow( () -> new ResourceNotFoundException( "Order not found with id: " + id ) );
        return mapToDto( order );
    }

    /**
     * Gets order by name
     *
     * @param name
     *            name of order
     * @return dto of the order
     */
    @Override
    public List<OrderDto> getOrderByName ( final String name ) {
        return orderRepository.findAll().stream().filter( order -> order.getName().equals( name ) )
                .map( this::mapToDto ).toList();
    }

    /**
     * Creates an order
     *
     * @param dto
     *            of the new order
     * @return dto of the created order
     */
    @Override
    public OrderDto createOrder ( final OrderDto dto ) {

        List<OrderLine> incomingLines = dto.getOrderLines();

        if ( incomingLines == null ) {
            incomingLines = List.of();
        }

        final Order order = new Order();
        order.setName( dto.getName() );

        final List<OrderLine> builtLine = new ArrayList<>();
        double subTotal = 0.0;

        for ( final OrderLine lineDto : incomingLines ) {
            final Item item = itemRepository.findById( lineDto.getItemId() ).orElseThrow(
                    () -> new ResourceNotFoundException( "Item not found with id: " + lineDto.getItemId() ) );

            final OrderLine built = new OrderLine();
            built.setItemId( item.getId() );
            built.setItemName( item.getName() );
            built.setPrice( item.getPrice() );
            built.setAmount( lineDto.getAmount() );

            builtLine.add( built );

            subTotal += item.getPrice() * lineDto.getAmount();
        }

        double currentTaxRate = 0.0;

        final List<Tax> taxes = taxRepository.findAll();
        if ( !taxes.isEmpty() ) {
            currentTaxRate = taxes.get( 0 ).getRate();
        }
        final double taxAmount = subTotal * ( currentTaxRate / 100.0 );

        final double tip = dto.getTip();

        final double total = subTotal + taxAmount + tip;

        order.setOrderLines( builtLine );
        order.setTip( tip );
        order.setTaxrate( currentTaxRate );
        order.setTotalPrice( total );
        order.setStatus( OrderStatus.PENDING );

        final Order saved = orderRepository.save( order );
        return mapToDto( saved );
    }

    /**
     * Updates an order
     *
     * @param id
     *            id of order to change
     * @param dto
     *            to make to order
     * @return updated order
     */
    @Override
    public OrderDto updateOrder ( final Long id, final OrderDto dto ) {
        // Find order
        final Order existing = orderRepository.findById( id )
                .orElseThrow( () -> new ResourceNotFoundException( "Order not found with ID: " + id ) );

        existing.setName( dto.getName() );

        // Get the existing lines
        List<OrderLine> incomingLines = dto.getOrderLines();
        if ( incomingLines == null ) {
            incomingLines = List.of();
        }

        // Rebuild order lines
        final List<OrderLine> rebuiltLines = new ArrayList<>();
        double total = 0.0;

        for ( final OrderLine lineDto : incomingLines ) {
            final Item item = itemRepository.findById( lineDto.getItemId() ).orElseThrow(
                    () -> new ResourceNotFoundException( "Item not found with ID: " + lineDto.getItemId() ) );

            // Remake snapshot
            final OrderLine rebuilt = new OrderLine();
            rebuilt.setItemId( item.getId() );
            rebuilt.setItemName( item.getName() );
            rebuilt.setPrice( item.getPrice() );
            rebuilt.setAmount( lineDto.getAmount() );

            rebuiltLines.add( rebuilt );

            total += item.getPrice() * lineDto.getAmount();
        }

        // Put new order lines in order
        existing.setOrderLines( rebuiltLines );

        double taxRate = 0;

        final List<Tax> taxes = taxRepository.findAll();
        if ( !taxes.isEmpty() ) {
            taxRate = taxes.get( 0 ).getRate() / 100.0;
        }

        final double finalTotal = total + ( total * taxRate );
        // Update price
        existing.setTotalPrice( finalTotal );

        final Order updated = orderRepository.save( existing );
        return mapToDto( updated );
    }

    /**
     * Deletes an order
     *
     * @param id
     *            of the order to delete
     */
    @Override
    public void deleteOrder ( final Long id ) {
        orderRepository.deleteById( id );
    }

    /**
     * Updates the status of an order
     *
     * @param id
     *            id of the order to update
     * @param status
     *            the new status of the order
     * @param role
     *            the role of the user updating the status
     * @param username
     *            of the user updating the role
     * @return the order with the new status
     */
    @Override
    public OrderDto updateStatus ( final Long id, final OrderStatus status, final String role, final String username ) {
        final Order order = orderRepository.findById( id )
                .orElseThrow( () -> new ResourceNotFoundException( "Order not found with ID: " + id ) );

        final OrderStatus current = order.getStatus();

        // Check if order is already picked up
        if ( current == OrderStatus.PICKED_UP ) {
            throw new IllegalStateException( "Order is already completed." );
        }

        // Check if admin or staff is fulfilling order and if there is
        // sufficient inventory for the order
        if ( status == OrderStatus.FULFILLED ) {
            if ( !"ROLE_STAFF".equals( role ) && !"ROLE_ADMIN".equals( role ) ) {
                throw new IllegalStateException( "Only staff or admin can fulfill orders" );
            }

            for ( final OrderLine line : order.getOrderLines() ) {
                final Item item = itemRepository.findById( line.getItemId() )
                        .orElseThrow( () -> new ResourceNotFoundException( "Item not found." ) );

                if ( item.getAmount() < line.getAmount() ) {
                    throw new IllegalStateException( "Not enough inventory for item: " + item.getName() );
                }

            }
        }

        // Decrement inventory only when fulfilling
        if ( status == OrderStatus.FULFILLED ) {
            for ( final OrderLine line : order.getOrderLines() ) {
                final Item item = itemRepository.findById( line.getItemId() )
                        .orElseThrow( () -> new ResourceNotFoundException( "Item not found." ) );

                item.setAmount( item.getAmount() - line.getAmount() );
                itemRepository.save( item );
            }
        }

        // Checks all requirements to pick up an order
        if ( status == OrderStatus.PICKED_UP )

        {
            // if ( !"ROLE_CUSTOMER".equals( role ) ) {
            // throw new IllegalStateException( "Only the customer can pick up
            // their order." );
            // }

            if ( !order.getName().equals( username ) ) {
                throw new IllegalStateException( "You can only pick up your own orders." );
            }

            if ( current != OrderStatus.FULFILLED ) {
                throw new IllegalStateException( "Order must be fulfilled before pickup" );
            }
        }

        order.setStatus( status );
        final Order saved = orderRepository.save( order );
        return mapToDto( saved );

    }

    /**
     * Gets tax rate
     *
     * @return tax
     */
    @Override
    public double getTax () {
        final List<Tax> taxes = taxRepository.findAll();
        if ( taxes.isEmpty() ) {
            return 0.0;
        }
        return taxes.get( 0 ).getRate();
    }

    /**
     * Sets tax rate
     *
     * @param rate
     *            tax to set
     */
    @Override
    public void setTax ( final double rate ) {
        taxRepository.deleteAll();

        final Tax tax = new Tax();
        tax.setRate( rate );
        taxRepository.save( tax );
    }

}
