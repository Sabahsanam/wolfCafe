package edu.ncsu.csc326.wolfcafe.service;

import edu.ncsu.csc326.wolfcafe.dto.ItemDto;

import java.util.List;

/**
 * Item service
 */
public interface ItemService {

    /**
     * Adds given item
     * 
     * @param itemDto
     *            item to add
     * @return added item
     */
    ItemDto addItem ( ItemDto itemDto );

    /**
     * Gets item by id
     * 
     * @param id
     *            id of item to get
     * @return returned item
     */
    ItemDto getItem ( Long id );

    /**
     * Returns all items
     * 
     * @return all items
     */
    List<ItemDto> getAllItems ();

    /**
     * Updates the item with the given id
     * 
     * @param id
     *            id of item to update
     * @param itemDto
     *            information of item to update
     * @return updated item
     */
    ItemDto updateItem ( Long id, ItemDto itemDto );

    /**
     * Deletes the item with the given id
     * 
     * @param id
     *            id of item to delete
     */
    void deleteItem ( Long id );
}
