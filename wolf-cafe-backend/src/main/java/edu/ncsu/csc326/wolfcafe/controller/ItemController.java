package edu.ncsu.csc326.wolfcafe.controller;

import edu.ncsu.csc326.wolfcafe.dto.ItemDto;
import edu.ncsu.csc326.wolfcafe.service.ItemService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for API endpoints for an Item TODO - update roles as appropriate
 * for requirements
 */
@RestController
@RequestMapping ( "api/items" )
@AllArgsConstructor
@CrossOrigin ( "*" )
public class ItemController {

    /** Link to ItemService */
    private ItemService itemService;

    /**
     * Adds an item to the list of items. Requires the STAFF or ADMIN role.
     * 
     * @param itemDto
     *            item to add
     * @return added item
     */
    @PreAuthorize ( "hasAnyRole('STAFF', 'ADMIN')" )
    @PostMapping
    public ResponseEntity<ItemDto> addItem ( @RequestBody ItemDto itemDto ) {
        ItemDto savedItem = itemService.addItem( itemDto );
        return new ResponseEntity<>( savedItem, HttpStatus.CREATED );
    }

    /**
     * Gets an item by id. Requires the ADMIN, STAFF or CUSTOMER role.
     * 
     * @param id
     *            item id
     * @return item with the id
     */
    @PreAuthorize ( "hasAnyRole('ADMIN', 'STAFF', 'CUSTOMER')" )
    @GetMapping ( "{id}" )
    public ResponseEntity<ItemDto> getItem ( @PathVariable ( "id" ) Long id ) {
        ItemDto item = itemService.getItem( id );
        return ResponseEntity.ok( item );
    }

    /**
     * Returns all items. Requires the ADMIN, STAFF or CUSTOMER role.
     * 
     * @return a list of all items
     */
    @PreAuthorize ( "hasAnyRole('ADMIN', 'STAFF', 'CUSTOMER')" )
    @GetMapping
    public ResponseEntity<List<ItemDto>> getAllItems () {
        List<ItemDto> items = itemService.getAllItems();
        return ResponseEntity.ok( items );
    }

    /**
     * Updates the item with the given id. Requires STAFF role.
     * 
     * @param id
     *            item to update
     * @param itemDto
     *            information about the item to update
     * @return updated item
     */
    @PreAuthorize ( "hasAnyRole('ADMIN','STAFF')" )
    @PutMapping ( "{id}" )
    public ResponseEntity<ItemDto> updateItem ( @PathVariable ( "id" ) Long id, @RequestBody ItemDto itemDto ) {
        ItemDto updatedItem = itemService.updateItem( id, itemDto );
        return ResponseEntity.ok( updatedItem );
    }

    /**
     * Deletes the item with the given id. Requires the STAFF role.
     * 
     * @param id
     *            item to delete
     * @return response indicating success or failure
     */
    @PreAuthorize ( "hasAnyRole('ADMIN','STAFF')" )
    @DeleteMapping ( "{id}" )
    public ResponseEntity<String> deleteItem ( @PathVariable ( "id" ) Long id ) {
        itemService.deleteItem( id );
        return ResponseEntity.ok( "Item deleted successfully" );
    }
}
