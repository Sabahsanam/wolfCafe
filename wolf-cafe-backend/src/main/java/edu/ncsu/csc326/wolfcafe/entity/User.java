package edu.ncsu.csc326.wolfcafe.entity;

import java.util.Collection;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * System user.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table ( name = "users" )
public class User {

    /** User's id */
    @Id
    @GeneratedValue ( strategy = GenerationType.IDENTITY )
    private Long             id;

    /** User's name */
    private String           name;

    /** User's username */
    @Column ( nullable = false, unique = true )
    private String           username;

    /** User's email */
    @Email ( message = "Invalid email address." )
    @Column ( nullable = false, unique = true )
    private String           email;

    /** User's password */
    @Column ( nullable = false )
    private String           password;

    /** User's roles */
    @ManyToMany ( fetch = FetchType.EAGER )
    @JoinTable ( name = "users_roles", joinColumns = @JoinColumn ( name = "user_id", referencedColumnName = "id" ),
            inverseJoinColumns = @JoinColumn ( name = "role_id", referencedColumnName = "id" ) )
    private Collection<Role> roles;
}
