package edu.ncsu.csc326.wolfcafe.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.ncsu.csc326.wolfcafe.entity.Item;

/**
 * Repository interface for Items.
 */
public interface ItemRepository extends JpaRepository<Item, Long> {

    /**
     * Method to search for an item by name
     *
     * @param name
     *            name of the item
     * @return the Item if it could be found
     */
    Optional<Item> findByName ( String name );
}
