package edu.ncsu.csc326.wolfcafe.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a customer order in the WolfCafe system.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table ( name = "orders" )
public class Order {

    /** Unique order ID */
    @Id
    @GeneratedValue ( strategy = GenerationType.IDENTITY )
    private Long            id;

    /** Customer name or order name */
    private String          name;

    /** Total price of the order */
    private double          totalPrice;

    /** Sets the default order status to pending */
    @Enumerated ( EnumType.STRING )
    private OrderStatus     status     = OrderStatus.PENDING;

    /** Tip on the order */
    private double          tip        = 0.0;

    /** The tax rate at the time of the order */
    private double          taxrate    = 0.0;

    /** The ordered items (as OrderLine structs) */
    @ElementCollection ( fetch = FetchType.EAGER )
    @CollectionTable ( name = "order_lines", joinColumns = @JoinColumn ( name = "order_id" ) )
    private List<OrderLine> orderLines = new ArrayList<>();

    /**
     * Constructor with no id for easier testing
     *
     * @param name
     *            name of order
     * @param totalPrice
     *            total price of order
     */
    public Order ( final String name, final double totalPrice ) {
        this.name = name;
        this.totalPrice = totalPrice;
    }

}
