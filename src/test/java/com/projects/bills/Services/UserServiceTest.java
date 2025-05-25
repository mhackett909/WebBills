package com.projects.bills.Services;

import com.projects.bills.DTOs.UserDTO;
import com.projects.bills.Entities.User;
import com.projects.bills.Enums.UpdateType;
import com.projects.bills.Mappers.UserMapper;
import com.projects.bills.Repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private UserRepository userRepository;
    private PasswordService passwordService;
    private JwtService jwtService;
    private UserMapper userMapper;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordService = mock(PasswordService.class);
        jwtService = mock(JwtService.class);
        userMapper = mock(UserMapper.class);
        userService = new UserService(userRepository, passwordService, jwtService, userMapper);
    }

    static Stream<Arguments> updateTypeProvider() {
        return Stream.of(
                Arguments.of(UpdateType.EMAIL, "new@email.com", null, null, null),
                Arguments.of(UpdateType.PASSWORD, null, "OldPass1!", "NewPass2@", null),
                Arguments.of(UpdateType.RECYCLE, null, null, null, true)
        );
    }

    @ParameterizedTest
    @MethodSource("updateTypeProvider")
    void testUpdateUser_UpdateType(UpdateType type, String newEmail, String password, String newPassword, Boolean recycle) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(1L);
        userDTO.setUsername("alice");
        userDTO.setPassword(password != null ? password : "OldPass1!");
        userDTO.setNewEmail(newEmail);
        userDTO.setNewPassword(newPassword);
        userDTO.setRecycle(recycle);

        User user = new User();
        user.setId(1L);
        user.setUsername("alice");
        user.setEmail("old@email.com");
        user.setPassword("hashed");
        user.setRecycleDate(null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordService.verifyPassword(any(), any())).thenReturn(true);
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(userMapper.mapToDTO(any())).thenReturn(userDTO);

        if (type == UpdateType.EMAIL) {
            when(userRepository.existsByEmail("new@email.com")).thenReturn(false);
        }
        if (type == UpdateType.PASSWORD) {
            when(passwordService.hashPassword("NewPass2@")).thenReturn("hashedNew");
        }

        UserDTO result = userService.updateUser(userDTO, "alice");
        assertNotNull(result);

        if (type == UpdateType.EMAIL) {
            assertEquals("new@email.com", user.getEmail());
        }
        if (type == UpdateType.PASSWORD) {
            assertEquals("hashedNew", user.getPassword());
        }
        if (type == UpdateType.RECYCLE) {
            assertNotNull(user.getRecycleDate());
        }
    }

    static Stream<Arguments> newPasswords() {
        String passwordException = "Password must be at least 8 characters long and include uppercase, lowercase, digit, and special character";
        return Stream.of(
                Arguments.of("OldPass1!", "OldPass1!", "New password cannot be the same as the current password"),
                Arguments.of("OldPass1!", "", "Current and new password are required"),
                Arguments.of("OldPass1!", "short", passwordException),
                Arguments.of("OldPass1!", "NoSpecial1", passwordException),
                Arguments.of("OldPass1!", "NoDigit!", passwordException),
                Arguments.of("OldPass1!", "nouppercase1!", passwordException),
                Arguments.of("OldPass1!", "NOLOWERCASE1!", passwordException),
                Arguments.of("OldPass1!", "Valid-New1-", null),
                Arguments.of("OldPass1!", "Password1@", null), // Common pattern, meets requirements
                Arguments.of("OldPass1!", "Summer2024$", null), // Season+year+special
                Arguments.of("OldPass1!", "Qwerty!234", null), // Keyboard pattern + special
                Arguments.of("OldPass1!", "MyDog$Bingo9", null), // Phrase + special + digit
                Arguments.of("OldPass1!", "XyZ!8vbnM@", null), // Mixed case, random
                Arguments.of("OldPass1!", "T!g7pL#2wQ", null), // Strong, random
                Arguments.of("OldPass1!", "3f$Gk!zP9q", null), // Strong, random
                Arguments.of("OldPass1!", "Aq1*sW2@dE", null), // Alternating case, digits, specials
                Arguments.of("OldPass1!", "r4Ndom$Tr1ng", null), // Random phrase, meets all
                Arguments.of("OldPass1!", "uY7!kLp2@zX", null) // Apple-style strong
        );
    }

    @ParameterizedTest
    @MethodSource("newPasswords")
    void testUpdateUser_PasswordStrength(String oldPassword, String newPassword, String expectedError) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(1L);
        userDTO.setUsername("alice");
        userDTO.setPassword(oldPassword);
        userDTO.setNewPassword(newPassword);

        User user = new User();
        user.setId(1L);
        user.setUsername("alice");
        user.setPassword("hashed");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordService.verifyPassword(any(), any())).thenReturn(true);

        if (expectedError == null) {
            when(passwordService.hashPassword(newPassword)).thenReturn("hashedNew");
            when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(userMapper.mapToDTO(any())).thenReturn(userDTO);

            UserDTO result = userService.updateUser(userDTO, "alice");
            assertNotNull(result);
        } else {
            ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                    userService.updateUser(userDTO, "alice"));
            assertTrue(Objects.requireNonNull(ex.getReason()).contains(expectedError));
        }
    }
}