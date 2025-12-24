package edu.ncsu.csc326.wolfcafe.repositories;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import edu.ncsu.csc326.wolfcafe.entity.Order;
import edu.ncsu.csc326.wolfcafe.repository.OrderRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

/**
 * Tests OrderRepository
 */
@DataJpaTest
@AutoConfigureTestDatabase ( replace = Replace.NONE )
class OrderRepositoryTest {

    /** OrderRepository for test */
    @Autowired
    private OrderRepository orderRepository;

    /** Entity Manager to help with tests */
    @Autowired
    private EntityManager   em;

    /** id 1 */
    private Long            order1Id;
    /** id 2 */
    private Long            order2Id;

    /**
     * Sets up test cases
     *
     * @throws Exception
     *             if setup cannot be completed
     */
    @BeforeEach
    public void setUp () throws Exception {
        orderRepository.deleteAll();
        em.flush();

        final Order order1 = new Order();
        order1.setName( "Morning Order" );
        order1.setTotalPrice( 12.50 );

        final Order order2 = new Order();
        order2.setName( "Evening Order" );
        order2.setTotalPrice( 8.75 );

        order1Id = orderRepository.save( order1 ).getId();
        order2Id = orderRepository.save( order2 ).getId();
    }

    /**
     * Test saving and retrieving orders
     */
    @Test
    @Transactional
    public void testAddAndRetrieveOrders () {
        final Order o1 = orderRepository.findById( order1Id ).get();
        assertAll( "Order 1 contents", () -> assertEquals( order1Id, o1.getId() ),
                () -> assertEquals( "Morning Order", o1.getName() ), () -> assertEquals( 12.50, o1.getTotalPrice() ) );

        final Order o2 = orderRepository.findById( order2Id ).get();
        assertAll( "Order 2 contents", () -> assertEquals( order2Id, o2.getId() ),
                () -> assertEquals( "Evening Order", o2.getName() ), () -> assertEquals( 8.75, o2.getTotalPrice() ) );
    }

    /**
     * Test findByName method
     */
    @Test
    @Transactional
    public void testFindByName () {
        final Order found = orderRepository.findByName( "Morning Order" ).orElseThrow();
        assertEquals( "Morning Order", found.getName() );
        assertEquals( 12.50, found.getTotalPrice() );
    }
}
