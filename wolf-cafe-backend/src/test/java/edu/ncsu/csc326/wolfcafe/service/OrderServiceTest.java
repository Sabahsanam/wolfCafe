package edu.ncsu.csc326.wolfcafe.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import edu.ncsu.csc326.wolfcafe.dto.OrderDto;
import edu.ncsu.csc326.wolfcafe.entity.Item;
import edu.ncsu.csc326.wolfcafe.entity.OrderLine;
import edu.ncsu.csc326.wolfcafe.entity.OrderStatus;
import edu.ncsu.csc326.wolfcafe.exception.ResourceNotFoundException;
import edu.ncsu.csc326.wolfcafe.repository.ItemRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

/**
 * Tests for OrderServiceImpl
 */
@SpringBootTest
public class OrderServiceTest {

    @Autowired
    private OrderService   orderService;

    @Autowired
    private EntityManager  entityManager;

    @Autowired
    private ItemRepository itemRepository;

    Item                   latte;
    Item                   espresso;

    @BeforeEach
    public void setUp () {
        final Query q = entityManager.createNativeQuery( "DELETE FROM orders" );
        q.executeUpdate();
        itemRepository.flush();
        itemRepository.deleteAll();
        latte = itemRepository.save( new Item( null, "Latte", "latte desc", 10, 3.00 ) );
        espresso = itemRepository.save( new Item( null, "Espresso", "espresso desc", 10, 4.00 ) );
    }

    /**
     * Helper function to make order for testing
     *
     * @return OrderDto of the new order
     */
    private OrderDto makeTestOrder () {
        final OrderLine l1 = new OrderLine( latte.getId(), 2, 0.0, null );
        final OrderLine l2 = new OrderLine( espresso.getId(), 1, 0.0, null );

        final OrderDto dto = new OrderDto();
        dto.setName( "TestOrder" );
        dto.setOrderLines( List.of( l1, l2 ) );
        return dto;
    }

    /**
     * Test create order
     */
    @Test
    @Transactional
    void testCreateOrder () {
        final OrderDto created = orderService.createOrder( makeTestOrder() );

        assertEquals( "TestOrder", created.getName() );
        assertEquals( 10.00, created.getTotalPrice() );
        assertEquals( 2, created.getOrderLines().size(), 2 );
        assertEquals( "Latte", created.getOrderLines().get( 0 ).getItemName() );
    }

    /**
     * Test get order by id
     */
    @Test
    @Transactional
    void testGetOrderById () {
        final OrderDto created = orderService.createOrder( makeTestOrder() );
        final OrderDto found = orderService.getOrderById( created.getId() );

        assertEquals( created.getId(), found.getId() );
        assertEquals( "TestOrder", found.getName() );
        assertEquals( 10.00, found.getTotalPrice() );
    }

    /**
     * Test get all orders
     */
    @Test
    @Transactional
    void testGetAllOrders () {
        orderService.createOrder( makeTestOrder() );
        orderService.createOrder( makeTestOrder() );

        final List<OrderDto> all = orderService.getAllOrders();
        assertEquals( 2, all.size() );
    }

    /**
     * Test update order
     */
    @Test
    @Transactional
    void testUpdateOrder () {
        final OrderDto created = orderService.createOrder( makeTestOrder() );

        final OrderLine newLine = new OrderLine( espresso.getId(), 2, 0.0, null );
        final OrderDto newData = new OrderDto();
        newData.setName( "Updated Order" );
        newData.setOrderLines( List.of( newLine ) );

        final OrderDto updated = orderService.updateOrder( created.getId(), newData );

        assertEquals( "Updated Order", updated.getName() );
        assertEquals( 8.00, updated.getTotalPrice() );
        assertEquals( 1, updated.getOrderLines().size() );
        assertEquals( "Espresso", updated.getOrderLines().get( 0 ).getItemName() );
    }

    /**
     * Test delete order
     */
    @Test
    @Transactional
    void testDeleteOrder () {
        final OrderDto created = orderService.createOrder( makeTestOrder() );

        orderService.deleteOrder( created.getId() );

        assertThrows( ResourceNotFoundException.class, () -> orderService.getOrderById( created.getId() ) );
    }

    /**
     * Ensure inventory is only decremented when staff/admin fulfill an order.
     */
    @Test
    @Transactional
    void testInventoryNotDecrementedOnPickup () {
        final OrderDto created = orderService.createOrder( makeTestOrder() );

        orderService.updateStatus( created.getId(), OrderStatus.FULFILLED, "ROLE_STAFF", "staff-user" );

        final Item latteAfterFulfill = itemRepository.findById( latte.getId() ).orElseThrow();
        final Item espressoAfterFulfill = itemRepository.findById( espresso.getId() ).orElseThrow();
        assertEquals( 8, latteAfterFulfill.getAmount(), "Latte inventory should drop by 2 after fulfillment" );
        assertEquals( 9, espressoAfterFulfill.getAmount(), "Espresso inventory should drop by 1 after fulfillment" );

        orderService.updateStatus( created.getId(), OrderStatus.PICKED_UP, "ROLE_CUSTOMER", created.getName() );

        final Item latteAfterPickup = itemRepository.findById( latte.getId() ).orElseThrow();
        final Item espressoAfterPickup = itemRepository.findById( espresso.getId() ).orElseThrow();

        assertEquals( 8, latteAfterPickup.getAmount(),
                "Inventory should not change when customer picks up a fulfilled order" );
        assertEquals( 9, espressoAfterPickup.getAmount(),
                "Inventory should not change when customer picks up a fulfilled order" );
    }
}
