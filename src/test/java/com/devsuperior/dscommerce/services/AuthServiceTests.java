package com.devsuperior.dscommerce.services;

import com.devsuperior.dscommerce.entities.User;
import com.devsuperior.dscommerce.factory.UserFactory;
import com.devsuperior.dscommerce.services.exceptions.ForbiddenException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
public class AuthServiceTests {

    @InjectMocks
    private AuthService service;

    @Mock
    private UserService userService;

    private User admin, selfClient, otherClient;

    @BeforeEach
    void setUp() {
        admin = UserFactory.createAdminUser();
        selfClient = UserFactory.createCustomClientUser(1L, "Bob");
        otherClient = UserFactory.createCustomClientUser(2L, "Ana");
    }

    @Test
    void validateSelfOrAdminShouldDoNothingWhenAdminLogged() {
        Mockito.when(userService.authenticated()).thenReturn(admin);
        Assertions.assertDoesNotThrow(() -> service.validateSelfOrAdmin(admin.getId()));
    }

    @Test
    void validateSelfOrAdminShouldDoNothingWhenSelfLogged() {
        Mockito.when(userService.authenticated()).thenReturn(selfClient);
        Assertions.assertDoesNotThrow(() -> service.validateSelfOrAdmin(selfClient.getId()));
    }

    @Test
    void validateSelfOrAdminShouldThrowsForbiddenExceptionWhenOtherClientLogged() {
        Mockito.when(userService.authenticated()).thenReturn(selfClient);
        Assertions.assertThrows(ForbiddenException.class, () -> service.validateSelfOrAdmin(otherClient.getId()));
    }
}
