package edu.ncsu.csc326.wolfcafe.controller;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.ncsu.csc326.wolfcafe.dto.OrderDto;
import edu.ncsu.csc326.wolfcafe.dto.TaxDto;
import edu.ncsu.csc326.wolfcafe.entity.Item;
import edu.ncsu.csc326.wolfcafe.entity.OrderLine;
import edu.ncsu.csc326.wolfcafe.entity.OrderStatus;
import edu.ncsu.csc326.wolfcafe.repository.ItemRepository;
import edu.ncsu.csc326.wolfcafe.repository.OrderRepository;
import edu.ncsu.csc326.wolfcafe.service.OrderService;
import jakarta.transaction.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@WithMockUser ( username = "admin", roles = "ADMIN" )
class OrderControllerTest {

    /** MockMvc */
    @Autowired
    private MockMvc             mockMvc;

    /** Mapper */
    @Autowired
    private ObjectMapper        mapper;

    /** Order Repository */
    @Autowired
    private OrderRepository     orderRepository;

    /** Item Repository */
    @Autowired
    private ItemRepository      itemRepository;

    /** Order Service */
    @Autowired
    private OrderService        orderService;

    /** Order Dto */
    private OrderDto            baseOrder;
    /** Item 1 */
    private Item                item1;
    /** Item 2 */
    private Item                item2;

    /** Tax Rate */
    private static final double INITIAL_TAX_RATE = 0.0;

    @BeforeEach
    void setUp () {
        orderRepository.deleteAll();
        itemRepository.deleteAll();

        item1 = itemRepository.save( new Item( null, "Latte", "Latte description", 10, 3.00 ) );
        item2 = itemRepository.save( new Item( null, "Espresso", "Espresso description", 10, 4.00 ) );

        final OrderLine l1 = new OrderLine( item1.getId(), 2, 0.0, null );
        final OrderLine l2 = new OrderLine( item2.getId(), 1, 0.0, null );

        baseOrder = new OrderDto();
        baseOrder.setOrderLines( List.of( l1, l2 ) );
    }

    @Test
    void testFulfillOrderEndpoint () throws Exception {
        final String createdJson = mockMvc
                .perform( post( "/api/orders" ).contentType( MediaType.APPLICATION_JSON )
                        .content( mapper.writeValueAsString( baseOrder ) ) )
                .andExpect( status().isOk() ).andReturn().getResponse().getContentAsString();

        final OrderDto created = mapper.readValue( createdJson, OrderDto.class );

        mockMvc.perform( put( "/api/orders/" + created.getId() + "/status" ).contentType( MediaType.APPLICATION_JSON )
                .content( mapper.writeValueAsString( OrderStatus.FULFILLED ) ) ).andExpect( status().isOk() )
                .andExpect( jsonPath( "$.status" ).value( "FULFILLED" ) );

        final Item latteAfter = itemRepository.findById( item1.getId() ).orElseThrow();
        final Item espressoAfter = itemRepository.findById( item2.getId() ).orElseThrow();

        org.junit.jupiter.api.Assertions.assertEquals( 8, latteAfter.getAmount(),
                "Latte inventory should decrement once after fulfillment" );
        org.junit.jupiter.api.Assertions.assertEquals( 9, espressoAfter.getAmount(),
                "Espresso inventory should decrement once after fulfillment" );
    }

    @Test
    @WithMockUser ( username = "customer", roles = "CUSTOMER" )
    void testPickupOrderEndpoint () throws Exception {
        final String createdJson = mockMvc
                .perform( post( "/api/orders" ).contentType( MediaType.APPLICATION_JSON )
                        .content( mapper.writeValueAsString( baseOrder ) ) )
                .andExpect( status().isOk() ).andReturn().getResponse().getContentAsString();

        final OrderDto created = mapper.readValue( createdJson, OrderDto.class );

        orderService.updateStatus( created.getId(), OrderStatus.FULFILLED, "ROLE_STAFF", "staff-user" );

        mockMvc.perform( put( "/api/orders/" + created.getId() + "/status" ).contentType( MediaType.APPLICATION_JSON )
                .content( mapper.writeValueAsString( OrderStatus.PICKED_UP ) ) ).andExpect( status().isOk() )
                .andExpect( jsonPath( "$.status" ).value( "PICKED_UP" ) );

        final Item latteAfterPickup = itemRepository.findById( item1.getId() ).orElseThrow();
        final Item espressoAfterPickup = itemRepository.findById( item2.getId() ).orElseThrow();

        org.junit.jupiter.api.Assertions.assertEquals( 8, latteAfterPickup.getAmount(),
                "Latte inventory should remain unchanged during pickup" );
        org.junit.jupiter.api.Assertions.assertEquals( 9, espressoAfterPickup.getAmount(),
                "Espresso inventory should remain unchanged during pickup" );
    }

    @Test
    void testCreateOrder () throws Exception {
        mockMvc.perform( post( "/api/orders" ).contentType( MediaType.APPLICATION_JSON )
                .content( mapper.writeValueAsString( baseOrder ) ) ).andExpect( status().isOk() )
                .andExpect( jsonPath( "$.name" ).value( "admin" ) )
                .andExpect( jsonPath( "$.totalPrice", closeTo( 10.0 * ( 1 + INITIAL_TAX_RATE ), 0.01 ) ) )
                .andExpect( jsonPath( "$.orderLines", hasSize( 2 ) ) )
                .andExpect( jsonPath( "$.orderLines[0].itemName" ).value( "Latte" ) )
                .andExpect( jsonPath( "$.orderLines[0].amount" ).value( 2 ) )
                .andExpect( jsonPath( "$.orderLines[0].price", closeTo( 3.0, 0.01 ) ) )
                .andExpect( jsonPath( "$.orderLines[1].itemName" ).value( "Espresso" ) )
                .andExpect( jsonPath( "$.orderLines[1].amount" ).value( 1 ) )
                .andExpect( jsonPath( "$.orderLines[1].price", closeTo( 4.0, 0.01 ) ) );
    }

    @Test
    void testGetAllOrders () throws Exception {
        mockMvc.perform( post( "/api/orders" ).contentType( MediaType.APPLICATION_JSON )
                .content( mapper.writeValueAsString( baseOrder ) ) ).andExpect( status().isOk() );

        mockMvc.perform( get( "/api/orders" ) ).andExpect( status().isOk() ).andExpect( jsonPath( "$", hasSize( 1 ) ) )
                .andExpect( jsonPath( "$[0].name" ).value( "admin" ) )
                .andExpect( jsonPath( "$[0].totalPrice", closeTo( 10.0 * ( 1 + INITIAL_TAX_RATE ), 0.01 ) ) )
                .andExpect( jsonPath( "$[0].orderLines[0].itemName" ).value( "Latte" ) );
    }

    @Test
    void testGetOrderById () throws Exception {
        final String createdJson = mockMvc
                .perform( post( "/api/orders" ).contentType( MediaType.APPLICATION_JSON )
                        .content( mapper.writeValueAsString( baseOrder ) ) )
                .andExpect( status().isOk() ).andReturn().getResponse().getContentAsString();

        final OrderDto created = mapper.readValue( createdJson, OrderDto.class );

        mockMvc.perform( get( "/api/orders/id/" + created.getId() ) ).andExpect( status().isOk() )
                .andExpect( jsonPath( "$.name" ).value( "admin" ) )
                .andExpect( jsonPath( "$.totalPrice", closeTo( 10.0 * ( 1 + INITIAL_TAX_RATE ), 0.01 ) ) )
                .andExpect( jsonPath( "$.orderLines[1].itemName" ).value( "Espresso" ) );
    }

    @Test
    void testUpdateOrder () throws Exception {
        final String createdJson = mockMvc
                .perform( post( "/api/orders" ).contentType( MediaType.APPLICATION_JSON )
                        .content( mapper.writeValueAsString( baseOrder ) ) )
                .andExpect( status().isOk() ).andReturn().getResponse().getContentAsString();

        final OrderDto created = mapper.readValue( createdJson, OrderDto.class );

        final OrderLine updatedLine = new OrderLine( item2.getId(), 2, 0.0, null );
        created.setOrderLines( List.of( updatedLine ) );

        mockMvc.perform( put( "/api/orders/" + created.getId() ).contentType( MediaType.APPLICATION_JSON )
                .content( mapper.writeValueAsString( created ) ) ).andExpect( status().isOk() )
                .andExpect( jsonPath( "$.totalPrice", closeTo( 8.0 * ( 1 + INITIAL_TAX_RATE ), 0.01 ) ) )
                .andExpect( jsonPath( "$.orderLines", hasSize( 1 ) ) )
                .andExpect( jsonPath( "$.orderLines[0].itemName" ).value( "Espresso" ) )
                .andExpect( jsonPath( "$.orderLines[0].amount" ).value( 2 ) );
    }

    @Test
    void testDeleteOrder () throws Exception {
        final String createdJson = mockMvc
                .perform( post( "/api/orders" ).contentType( MediaType.APPLICATION_JSON )
                        .content( mapper.writeValueAsString( baseOrder ) ) )
                .andExpect( status().isOk() ).andReturn().getResponse().getContentAsString();

        final OrderDto created = mapper.readValue( createdJson, OrderDto.class );

        mockMvc.perform( delete( "/api/orders/" + created.getId() ) ).andExpect( status().isOk() );

        mockMvc.perform( get( "/api/orders/id/" + created.getId() ) ).andExpect( status().is4xxClientError() );
    }

    @Test
    void testSetTax () throws Exception {
        final TaxDto taxDto = new TaxDto();
        taxDto.setRate( 10 );

        mockMvc.perform( put( "/api/orders/tax" ).contentType( MediaType.APPLICATION_JSON )
                .content( mapper.writeValueAsString( taxDto ) ) ).andExpect( status().isOk() );

        mockMvc.perform( get( "/api/orders/tax" ) ).andExpect( status().isOk() )
                .andExpect( jsonPath( "$.rate", closeTo( 10, 10 ) ) );
    }

    @Test
    void testSetNegativeTax () throws Exception {
        final TaxDto taxDto = new TaxDto();
        taxDto.setRate( -1.0 ); // any negative value

        mockMvc.perform( put( "/api/orders/tax" ).contentType( MediaType.APPLICATION_JSON )
                .content( mapper.writeValueAsString( taxDto ) ) ).andExpect( status().isBadRequest() );
    }
}
