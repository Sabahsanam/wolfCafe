package edu.ncsu.csc326.wolfcafe.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.List;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.ncsu.csc326.wolfcafe.WolfCafeApplication;
import edu.ncsu.csc326.wolfcafe.dto.ItemDto;
import edu.ncsu.csc326.wolfcafe.service.ItemService;

/**
 * Tests the ItemController
 */
@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration ( classes = WolfCafeApplication.class )
public class ItemControllerTest {

    /** MVC */
    @Autowired
    private MockMvc                   mvc;

    /** Item Service */
    @MockitoBean
    private ItemService               itemService;

    /** Mapper */
    private static final ObjectMapper MAPPER      = new ObjectMapper();

    /** Path */
    private static final String       API_PATH    = "/api/items";
    /** Encoding */
    private static final String       ENCODING    = "utf-8";

    /** Name */
    private static final String       ITEM_NAME   = "Coffee";
    /** Description */
    private static final String       ITEM_DESC   = "Test description";
    /** Amount */
    private static final Integer      ITEM_AMOUNT = 10;
    /** Price */
    private static final double       ITEM_PRICE  = 3.25;

    /**
     * Test creating an item as STAFF
     */
    @Test
    @WithMockUser ( username = "staff", roles = "STAFF" )
    public void testCreateItem () throws Exception {
        final ItemDto itemDto = new ItemDto( null, ITEM_NAME, ITEM_DESC, ITEM_AMOUNT, ITEM_PRICE );

        Mockito.when( itemService.addItem( ArgumentMatchers.any() ) ).thenReturn( itemDto );

        final String json = MAPPER.writeValueAsString( itemDto );

        mvc.perform( post( API_PATH ).contentType( MediaType.APPLICATION_JSON ).characterEncoding( ENCODING )
                .content( json ).accept( MediaType.APPLICATION_JSON ) ).andExpect( status().isCreated() )
                .andExpect( jsonPath( "$.name", Matchers.equalTo( ITEM_NAME ) ) )
                .andExpect( jsonPath( "$.description", Matchers.equalTo( ITEM_DESC ) ) )
                .andExpect( jsonPath( "$.amount", Matchers.equalTo( ITEM_AMOUNT ) ) )
                .andExpect( jsonPath( "$.price", Matchers.equalTo( ITEM_PRICE ) ) );
    }

    /**
     * Unauthorized create attempt
     */
    @Test
    public void testCreateItemNotAuthorized () throws Exception {
        final ItemDto itemDto = new ItemDto( null, ITEM_NAME, ITEM_DESC, ITEM_AMOUNT, ITEM_PRICE );
        final String json = MAPPER.writeValueAsString( itemDto );

        mvc.perform( post( API_PATH ).contentType( MediaType.APPLICATION_JSON ).characterEncoding( ENCODING )
                .content( json ).accept( MediaType.APPLICATION_JSON ) ).andExpect( status().isUnauthorized() );
    }

    /**
     * Test getting item by ID
     */
    @Test
    @WithMockUser ( username = "staff", roles = "STAFF" )
    public void testGetItemById () throws Exception {
        final ItemDto itemDto = new ItemDto( 27L, ITEM_NAME, ITEM_DESC, ITEM_AMOUNT, ITEM_PRICE );

        Mockito.when( itemService.getItem( ArgumentMatchers.any() ) ).thenReturn( itemDto );

        mvc.perform(
                get( API_PATH + "/27" ).contentType( MediaType.APPLICATION_JSON ).accept( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isOk() ).andExpect( jsonPath( "$.id", Matchers.equalTo( 27 ) ) )
                .andExpect( jsonPath( "$.name", Matchers.equalTo( ITEM_NAME ) ) )
                .andExpect( jsonPath( "$.description", Matchers.equalTo( ITEM_DESC ) ) )
                .andExpect( jsonPath( "$.amount", Matchers.equalTo( ITEM_AMOUNT ) ) )
                .andExpect( jsonPath( "$.price", Matchers.equalTo( ITEM_PRICE ) ) );
    }

    /**
     * Test retrieving all items
     */
    @Test
    @WithMockUser ( username = "customer", roles = "CUSTOMER" )
    public void testGetAllItems () throws Exception {
        final ItemDto item1 = new ItemDto( 1L, "Coffee", "Desc1", 10, 3.25 );
        final ItemDto item2 = new ItemDto( 2L, "Latte", "Desc2", 5, 4.75 );

        final List<ItemDto> items = Arrays.asList( item1, item2 );

        Mockito.when( itemService.getAllItems() ).thenReturn( items );

        mvc.perform( get( API_PATH ).contentType( MediaType.APPLICATION_JSON ).accept( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isOk() ).andExpect( jsonPath( "$[0].id", Matchers.equalTo( 1 ) ) )
                .andExpect( jsonPath( "$[0].name", Matchers.equalTo( "Coffee" ) ) )
                .andExpect( jsonPath( "$[0].description", Matchers.equalTo( "Desc1" ) ) )
                .andExpect( jsonPath( "$[1].id", Matchers.equalTo( 2 ) ) )
                .andExpect( jsonPath( "$[1].name", Matchers.equalTo( "Latte" ) ) )
                .andExpect( jsonPath( "$[1].description", Matchers.equalTo( "Desc2" ) ) );
    }

    /**
     * Test updating an item (ADMIN only)
     */
    @Test
    @WithMockUser ( username = "admin", roles = "ADMIN" )
    public void testUpdateItem () throws Exception {
        final ItemDto updated = new ItemDto( 27L, ITEM_NAME, ITEM_DESC, ITEM_AMOUNT, ITEM_PRICE );

        Mockito.when( itemService.updateItem( ArgumentMatchers.eq( 27L ), ArgumentMatchers.any() ) )
                .thenReturn( updated );

        final String json = MAPPER.writeValueAsString( updated );

        mvc.perform( put( API_PATH + "/27" ).contentType( MediaType.APPLICATION_JSON ).characterEncoding( ENCODING )
                .content( json ).accept( MediaType.APPLICATION_JSON ) ).andExpect( status().isOk() )
                .andExpect( jsonPath( "$.id", Matchers.equalTo( 27 ) ) )
                .andExpect( jsonPath( "$.description", Matchers.equalTo( ITEM_DESC ) ) );
    }

    /**
     * Unauthorized update attempt (customers cannot update)
     */
    @Test
    @WithMockUser ( username = "customer", roles = "CUSTOMER" )
    public void testUpdateItemNotAuthorized () throws Exception {
        final ItemDto dto = new ItemDto( 27L, ITEM_NAME, ITEM_DESC, ITEM_AMOUNT, ITEM_PRICE );

        final String json = MAPPER.writeValueAsString( dto );

        mvc.perform( put( API_PATH + "/27" ).contentType( MediaType.APPLICATION_JSON ).characterEncoding( ENCODING )
                .content( json ).accept( MediaType.APPLICATION_JSON ) ).andExpect( status().isForbidden() );
    }

    /**
     * Test delete item (ADMIN only)
     */
    @Test
    @WithMockUser ( username = "admin", roles = "ADMIN" )
    public void testDeleteItem () throws Exception {
        mvc.perform(
                delete( API_PATH + "/27" ).contentType( MediaType.APPLICATION_JSON ).characterEncoding( ENCODING ) )
                .andExpect( status().isOk() )
                .andExpect( jsonPath( "$", Matchers.equalTo( "Item deleted successfully" ) ) );
    }

    /**
     * Unauthorized delete attempt
     */
    @Test
    @WithMockUser ( username = "customer", roles = "CUSTOMER" )
    public void testDeleteItemNotAuthorized () throws Exception {
        mvc.perform( delete( API_PATH + "/27" ).contentType( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isForbidden() );
    }
}
