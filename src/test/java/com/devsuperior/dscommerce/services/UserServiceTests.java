package com.devsuperior.dscommerce.services;

import com.devsuperior.dscommerce.dto.UserDTO;
import com.devsuperior.dscommerce.entities.User;
import com.devsuperior.dscommerce.factory.UserDetailsFactory;
import com.devsuperior.dscommerce.factory.UserFactory;
import com.devsuperior.dscommerce.projections.UserDetailsProjection;
import com.devsuperior.dscommerce.repositories.UserRepository;
import com.devsuperior.dscommerce.util.CustomUserUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ExtendWith(SpringExtension.class)
public class UserServiceTests {

    @InjectMocks
    private UserService service;

    @Mock
    private UserRepository repository;

    @Mock
    private CustomUserUtil userUtil;

    private List<UserDetailsProjection> userDetails;
    private User user;
    private String existingUsername;
    private String nonExistingUsername;


    @BeforeEach
    void setUp() throws Exception {
        existingUsername = "maria@gmail.com";
        nonExistingUsername = "user@gmail.com";
        user = UserFactory.createCustomClientUser(1L, existingUsername);
        userDetails = UserDetailsFactory.createCustomAdminUser(existingUsername);

        Mockito.when(repository.searchUserAndRolesByEmail(existingUsername)).thenReturn(userDetails);
        Mockito.when(repository.searchUserAndRolesByEmail(nonExistingUsername)).thenReturn(new ArrayList<>());
        Mockito.when(repository.findByEmail(existingUsername)).thenReturn(Optional.of(user));
        Mockito.when(repository.findByEmail(nonExistingUsername)).thenReturn(Optional.empty());
    }

    @Test
    void loadUserByUsernameShouldReturnUserDetailWhenUserExists() {
        UserDetails result = service.loadUserByUsername(existingUsername);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(existingUsername, result.getUsername());
    }

    @Test
    void loadUserByUsernameShouldThrowUsernameNotFoundExceptionWhenUserDoesNotExists() {
        Assertions.assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername(nonExistingUsername));
    }

    @Test
    void authenticatedShouldReturnUserWhenUserExists() {
        Mockito.when(userUtil.getLoggedUsername()).thenReturn(existingUsername);
        User result = service.authenticated();
        Assertions.assertNotNull(result);
        Assertions.assertEquals(existingUsername, result.getUsername());
    }

    @Test
    void authenticatedShouldThrowsUsernameNotFoundExceptionWhenUserDoesNotExists() {
        Mockito.doThrow(ClassCastException.class).when(userUtil).getLoggedUsername();
        Assertions.assertThrows(UsernameNotFoundException.class, () -> service.authenticated());
    }

    @Test
    void getMeShouldReturnUserDTOWhenUserAuthenticated() {
        UserService spyUserService = Mockito.spy(service);
        Mockito.doReturn(user).when(spyUserService).authenticated();

        UserDTO result = spyUserService.getMe();
        Assertions.assertNotNull(result);
        Assertions.assertEquals(existingUsername, result.getEmail());
    }

    @Test
    void getMeShouldThrowsUsernameNotFoundExceptionWhenUserNotAuthenticated() {
        UserService spyUserService = Mockito.spy(service);
        Mockito.doThrow(UsernameNotFoundException.class).when(spyUserService).authenticated();
        Assertions.assertThrows(UsernameNotFoundException.class, spyUserService::getMe);
    }


}
