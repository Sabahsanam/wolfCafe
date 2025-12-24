package edu.ncsu.csc326.wolfcafe.service;

import edu.ncsu.csc326.wolfcafe.dto.RegisterDto;
import edu.ncsu.csc326.wolfcafe.entity.User;

/**
 * Interface defining the user registration behaviors.
 */
public interface UserRegistrationService {

    /**
     * Creates a user with the given information.
     *
     * @param registerDto
     *            user to create
     * @return created user
     */
    User register ( RegisterDto registerDto );
}
