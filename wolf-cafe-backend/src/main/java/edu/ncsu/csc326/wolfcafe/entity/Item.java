package edu.ncsu.csc326.wolfcafe.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents an item for sale in the WolfCafe.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table ( name = "items" )
public class Item {

    /** Item id */
    @Id
    @GeneratedValue ( strategy = GenerationType.IDENTITY )
    private Long    id;

    /** Item name */
    @Column ( nullable = false, unique = true )
    private String  name;

    /** Item description */
    private String  description;

    /** Item amount */
    @Min ( value = 0, message = "Item amount cannot be negative." )
    private Integer amount;

    /** Item price */
    @Column ( nullable = false )
    private double  price;

}
