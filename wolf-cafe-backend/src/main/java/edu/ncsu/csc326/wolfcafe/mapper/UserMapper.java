package edu.ncsu.csc326.wolfcafe.mapper;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import edu.ncsu.csc326.wolfcafe.dto.UserDto;
import edu.ncsu.csc326.wolfcafe.entity.Role;
import edu.ncsu.csc326.wolfcafe.entity.User;

/**
 * Converts user to userDto
 */
public final class UserMapper {

    /**
     * Constructor for mapper
     */
    private UserMapper () {

    }

    /**
     * Maps a user to a userDTO
     *
     * @param user
     *            user entity
     * @return DTO equivalent
     */
    public static UserDto mapToUserDto ( final User user ) {
        if ( user == null ) {
            return null;
        }

        List<String> roleNames = Collections.emptyList();
        if ( user.getRoles() != null ) {
            roleNames = user.getRoles().stream().map( Role::getName ).collect( Collectors.toList() );
        }

        final UserDto dto = new UserDto();
        dto.setId( user.getId() );
        dto.setName( user.getName() );
        dto.setUsername( user.getUsername() );
        dto.setEmail( user.getEmail() );
        dto.setRoles( roleNames );

        return dto;
    }
}
