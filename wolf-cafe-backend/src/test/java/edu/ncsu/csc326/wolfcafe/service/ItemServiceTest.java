package edu.ncsu.csc326.wolfcafe.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import edu.ncsu.csc326.wolfcafe.dto.ItemDto;
import edu.ncsu.csc326.wolfcafe.exception.ResourceNotFoundException;
import jakarta.persistence.EntityManager;

/**
 * Tests ItemServiceImpl
 */
@SpringBootTest
public class ItemServiceTest {

    /** Reference to ItemService */
    @Autowired
    private ItemService          itemService;

    /** Reference to EntityManager */
    @Autowired
    private EntityManager        entityManager;

    /** Item name */
    private static final String  ITEM_NAME   = "Coffee";
    /** Item description */
    private static final Integer ITEM_AMOUNT = 10;
    /** Item price */
    private static final double  ITEM_PRICE  = 3.25;

    /**
     * Sets up the test case.
     *
     * @throws java.lang.Exception
     *             if error
     */
    @BeforeEach
    public void setUp () throws Exception {
        entityManager.createNativeQuery( "DELETE FROM order_lines" ).executeUpdate();
        entityManager.createNativeQuery( "DELETE FROM orders" ).executeUpdate();
        entityManager.createNativeQuery( "DELETE FROM items" ).executeUpdate();
    }

    /**
     * Tests creating an item
     */
    @Test
    @Transactional
    void testCreateItem () {
        // Create ItemDto with all contents but the id
        final ItemDto itemDto = new ItemDto();
        itemDto.setName( ITEM_NAME );
        itemDto.setAmount( ITEM_AMOUNT );
        itemDto.setPrice( ITEM_PRICE );

        final ItemDto createdItemDto = itemService.addItem( itemDto );
        assertAll( "ItemDto contents", () -> assertEquals( ITEM_NAME, createdItemDto.getName() ),
                () -> assertEquals( ITEM_AMOUNT, createdItemDto.getAmount() ),
                () -> assertEquals( ITEM_PRICE, createdItemDto.getPrice() ) );
    }

    /**
     * Tests creating an item
     */
    @Test
    @Transactional
    void testGetItem () {
        // Create ItemDto with all contents but the id
        final ItemDto itemDto = new ItemDto();
        itemDto.setName( ITEM_NAME );
        itemDto.setAmount( ITEM_AMOUNT );
        itemDto.setPrice( ITEM_PRICE );

        final ItemDto createdItemDto = itemService.addItem( itemDto );

        final ItemDto retrievedItemDto = itemService.getItem( createdItemDto.getId() );
        assertAll( "ItemDto contents", () -> assertEquals( ITEM_NAME, retrievedItemDto.getName() ),
                () -> assertEquals( ITEM_AMOUNT, retrievedItemDto.getAmount() ),
                () -> assertEquals( ITEM_PRICE, retrievedItemDto.getPrice() ) );
    }

    /**
     * Tests creating an item
     */
    @Test
    @Transactional
    void testGetItemException () {
        // Create ItemDto with all contents but the id
        final ItemDto itemDto = new ItemDto();
        itemDto.setName( ITEM_NAME );
        itemDto.setAmount( ITEM_AMOUNT );
        itemDto.setPrice( ITEM_PRICE );

        final ItemDto createdItemDto = itemService.addItem( itemDto );

        assertThrows( ResourceNotFoundException.class, () -> itemService.getItem( createdItemDto.getId() + 1 ) );
    }

    /**
     * Tests creating an item
     */
    @Test
    @Transactional
    void testUpdateItem () {
        // Create ItemDto with all contents but the id
        final ItemDto itemDto = new ItemDto();
        itemDto.setName( ITEM_NAME );
        itemDto.setAmount( ITEM_AMOUNT );
        itemDto.setPrice( ITEM_PRICE );

        final ItemDto createdItemDto = itemService.addItem( itemDto );

        final ItemDto itemDto2 = new ItemDto();
        itemDto2.setName( "Latte" );
        itemDto2.setAmount( ITEM_AMOUNT );
        itemDto2.setPrice( 3.57 );

        final ItemDto updatedItemDto = itemService.updateItem( createdItemDto.getId(), itemDto2 );
        assertAll( "ItemDto contents", () -> assertEquals( "Latte", updatedItemDto.getName() ),
                () -> assertEquals( ITEM_AMOUNT, updatedItemDto.getAmount() ),
                () -> assertEquals( 3.57, updatedItemDto.getPrice() ) );
    }

    /**
     * Tests creating an item
     */
    @Test
    @Transactional
    void testUpdateItemException () {
        // Create ItemDto with all contents but the id
        final ItemDto itemDto = new ItemDto();
        itemDto.setName( ITEM_NAME );
        itemDto.setAmount( ITEM_AMOUNT );
        itemDto.setPrice( ITEM_PRICE );

        final ItemDto createdItemDto = itemService.addItem( itemDto );

        final ItemDto itemDto2 = new ItemDto();
        itemDto2.setName( "Latte" );
        itemDto2.setAmount( ITEM_AMOUNT );
        itemDto2.setPrice( 3.57 );

        assertThrows( ResourceNotFoundException.class,
                () -> itemService.updateItem( createdItemDto.getId() + 1, itemDto2 ) );
    }

    /**
     * Tests creating an item
     */
    @Test
    @Transactional
    void testDeleteItem () {
        // Create ItemDto with all contents but the id
        final ItemDto itemDto = new ItemDto();
        itemDto.setName( ITEM_NAME );
        itemDto.setAmount( ITEM_AMOUNT );
        itemDto.setPrice( ITEM_PRICE );

        final ItemDto createdItemDto = itemService.addItem( itemDto );

        itemService.deleteItem( createdItemDto.getId() );
        assertThrows( ResourceNotFoundException.class, () -> itemService.getItem( createdItemDto.getId() ) );
    }
}
