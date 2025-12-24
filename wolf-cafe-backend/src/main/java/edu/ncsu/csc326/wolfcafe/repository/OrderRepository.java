package edu.ncsu.csc326.wolfcafe.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import edu.ncsu.csc326.wolfcafe.entity.Order;

/**
 * Repository interface for Orders.
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Finds the order by name
     *
     * @param name
     *            name of the order
     * @return The order that was found
     */
    Optional<Order> findByName ( String name );
}
