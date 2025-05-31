package com.projects.bills.Services;

import com.projects.bills.Constants.Exceptions;
import com.projects.bills.DTOs.AuthDTO;
import com.projects.bills.DTOs.UserDTO;
import com.projects.bills.Entities.User;
import com.projects.bills.Enums.UpdateType;
import com.projects.bills.Mappers.UserMapper;
import com.projects.bills.Repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

import org.springframework.web.server.ResponseStatusException;

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
        return Stream.of(
                Arguments.of("OldPass1!", "OldPass1!", Exceptions.NEW_PASSWORD_SAME_AS_CURRENT),
                Arguments.of("OldPass1!", "", Exceptions.CURRENT_AND_NEW_PASSWORD_REQUIRED),
                Arguments.of("OldPass1!", "short", Exceptions.PASSWORD_STRENGTH),
                Arguments.of("OldPass1!", "NoSpecial1", Exceptions.PASSWORD_STRENGTH),
                Arguments.of("OldPass1!", "NoDigit!", Exceptions.PASSWORD_STRENGTH),
                Arguments.of("OldPass1!", "nouppercase1!", Exceptions.PASSWORD_STRENGTH),
                Arguments.of("OldPass1!", "NOLOWERCASE1!", Exceptions.PASSWORD_STRENGTH),
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
            assertThrows(ResponseStatusException.class, () ->
                    userService.updateUser(userDTO, "alice"));
        }
    }

    @Test
    void testRegisterUser_Success() {
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("alice");
        userDTO.setEmail("alice@email.com");
        userDTO.setPassword("ValidPass1!");

        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(userRepository.existsByEmail("alice@email.com")).thenReturn(false);
        when(passwordService.hashPassword("ValidPass1!")).thenReturn("hashed");
        User user = new User();
        user.setUsername("alice");
        user.setEmail("alice@email.com");
        user.setPassword("hashed");
        when(userMapper.mapToEntity(userDTO, "hashed")).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        UserDTO mappedDTO = new UserDTO();
        mappedDTO.setUsername("alice");
        mappedDTO.setEmail("alice@email.com");
        when(userMapper.mapToDTO(user)).thenReturn(mappedDTO);

        UserDTO result = userService.registerUser(userDTO);
        assertEquals("alice", result.getUsername());
        assertEquals("alice@email.com", result.getEmail());
    }

    // Successful login
    @Test
    void testLogin_Success() {
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("alice");
        userDTO.setPassword("ValidPass1!");

        User user = new User();
        user.setUsername("alice");
        user.setPassword("hashed");
        user.setRoles("ROLE_USER");

        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(passwordService.verifyPassword("ValidPass1!", "hashed")).thenReturn(true);
        when(userRepository.save(any())).thenReturn(user);
        when(jwtService.generateAccessToken(eq("alice"), anyList())).thenReturn("access-token");
        when(jwtService.generateRefreshToken(eq("alice"), anyList())).thenReturn("refresh-token");

        AuthDTO auth = userService.login(userDTO);
        assertEquals("alice", auth.getUsername());
        assertEquals("access-token", auth.getAccessToken());
        assertEquals("refresh-token", auth.getRefreshToken());
    }

    @Test
    void testRegisterUser_UsernameExists() {
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("alice");
        userDTO.setEmail("alice@email.com");
        userDTO.setPassword("ValidPass1!");

        when(userRepository.existsByUsername("alice")).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                userService.registerUser(userDTO));
        assertEquals(409, ex.getStatusCode().value());
        assertEquals(Exceptions.USERNAME_ALREADY_EXISTS, ex.getReason());
    }

    @Test
    void testRegisterUser_EmailExists() {
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("bob");
        userDTO.setEmail("bob@email.com");
        userDTO.setPassword("ValidPass1!");

        when(userRepository.existsByUsername("bob")).thenReturn(false);
        when(userRepository.existsByEmail("bob@email.com")).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                userService.registerUser(userDTO));
        assertEquals(409, ex.getStatusCode().value());
        assertEquals(Exceptions.EMAIL_ALREADY_REGISTERED, ex.getReason());
    }

    @Test
    void testRegisterUser_InvalidEmail() {
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("bob");
        userDTO.setEmail("bademail");
        userDTO.setPassword("ValidPass1!");

        when(userRepository.existsByUsername("bob")).thenReturn(false);
        when(userRepository.existsByEmail("bademail")).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                userService.registerUser(userDTO));
        assertEquals(400, ex.getStatusCode().value());
        assertEquals(Exceptions.INVALID_EMAIL_FORMAT, ex.getReason());
    }

    @Test
    void testLogin_UsernameNotFound() {
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("notfound");
        userDTO.setPassword("ValidPass1!");

        when(userRepository.findByUsername("notfound")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("notfound")).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                userService.login(userDTO));
        assertEquals(401, ex.getStatusCode().value());
        assertEquals(Exceptions.LOGIN_FAILED, ex.getReason());
    }

    @Test
    void testLogin_WrongPassword() {
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("alice");
        userDTO.setPassword("WrongPass1!");

        User user = new User();
        user.setUsername("alice");
        user.setPassword("hashed");

        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(passwordService.verifyPassword("WrongPass1!", "hashed")).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                userService.login(userDTO));
        assertEquals(401, ex.getStatusCode().value());
        assertEquals(Exceptions.LOGIN_FAILED, ex.getReason());
    }

    @Test
    void testUpdateUser_MissingId() {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(null);
        userDTO.setUsername("alice");
        userDTO.setPassword("ValidPass1!");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                userService.updateUser(userDTO, "alice"));
        assertEquals(400, ex.getStatusCode().value());
        assertEquals(Exceptions.USER_ID_REQUIRED_FOR_UPDATE, ex.getReason());
    }

    @Test
    void testUpdateUser_UserNotFound() {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(1L);
        userDTO.setUsername("alice");
        userDTO.setPassword("ValidPass1!");

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                userService.updateUser(userDTO, "alice"));
        assertEquals(404, ex.getStatusCode().value());
        assertEquals(String.format(Exceptions.USER_NOT_FOUND, 1L), ex.getReason());
    }

    @Test
    void testUpdateUser_Unauthorized() {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(1L);
        userDTO.setUsername("alice");
        userDTO.setPassword("ValidPass1!");

        User user = new User();
        user.setId(1L);
        user.setUsername("bob");
        user.setPassword("hashed");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                userService.updateUser(userDTO, "alice"));
        assertEquals(403, ex.getStatusCode().value());
        assertEquals(Exceptions.NOT_AUTHORIZED_TO_UPDATE_USER, ex.getReason());
    }

    @Test
    void testUpdateUser_WrongPassword() {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(1L);
        userDTO.setUsername("alice");
        userDTO.setPassword("WrongPass1!");

        User user = new User();
        user.setId(1L);
        user.setUsername("alice");
        user.setPassword("hashed");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordService.verifyPassword("WrongPass1!", "hashed")).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                userService.updateUser(userDTO, "alice"));
        assertEquals(401, ex.getStatusCode().value());
        assertEquals(Exceptions.LOGIN_FAILED, ex.getReason());
    }

    @Test
    void testUpdateUser_InvalidUpdateType() {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(1L);
        userDTO.setUsername("alice");
        userDTO.setPassword("ValidPass1!");

        User user = new User();
        user.setId(1L);
        user.setUsername("alice");
        user.setPassword("hashed");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordService.verifyPassword(any(), any())).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                userService.updateUser(userDTO, "alice"));
        assertEquals(400, ex.getStatusCode().value());
        assertEquals(Exceptions.NO_VALID_UPDATE_OPERATION, ex.getReason());
    }

    @Test
    void testUpdateUser_EmailAlreadyExists() {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(1L);
        userDTO.setUsername("alice");
        userDTO.setPassword("ValidPass1!");
        userDTO.setNewEmail("new@email.com");

        User user = new User();
        user.setId(1L);
        user.setUsername("alice");
        user.setEmail("old@email.com");
        user.setPassword("hashed");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordService.verifyPassword(any(), any())).thenReturn(true);
        when(userRepository.existsByEmail("new@email.com")).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                userService.updateUser(userDTO, "alice"));
        assertEquals(409, ex.getStatusCode().value());
        assertEquals(Exceptions.EMAIL_ALREADY_REGISTERED, ex.getReason());
    }

    @Test
    void testUpdateUser_NewEmailSameAsOld() {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(1L);
        userDTO.setUsername("alice");
        userDTO.setPassword("ValidPass1!");
        userDTO.setNewEmail("old@email.com");

        User user = new User();
        user.setId(1L);
        user.setUsername("alice");
        user.setEmail("old@email.com");
        user.setPassword("hashed");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordService.verifyPassword(any(), any())).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                userService.updateUser(userDTO, "alice"));
        assertEquals(400, ex.getStatusCode().value());
        assertEquals(Exceptions.NEW_EMAIL_SAME_AS_CURRENT, ex.getReason());
    }

    @Test
    void testUpdateUser_NewEmailMissing() {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(1L);
        userDTO.setUsername("alice");
        userDTO.setPassword("ValidPass1!");
        userDTO.setNewEmail("");

        User user = new User();
        user.setId(1L);
        user.setUsername("alice");
        user.setEmail("old@email.com");
        user.setPassword("hashed");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordService.verifyPassword(any(), any())).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                userService.updateUser(userDTO, "alice"));
        assertEquals(400, ex.getStatusCode().value());
        assertEquals(Exceptions.NEW_EMAIL_REQUIRED, ex.getReason());
    }

    @Test
    void testUpdateUser_InvalidEmailFormat() {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(1L);
        userDTO.setUsername("alice");
        userDTO.setPassword("ValidPass1!");
        userDTO.setNewEmail("bademail");

        User user = new User();
        user.setId(1L);
        user.setUsername("alice");
        user.setEmail("old@email.com");
        user.setPassword("hashed");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordService.verifyPassword(any(), any())).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                userService.updateUser(userDTO, "alice"));
        assertEquals(400, ex.getStatusCode().value());
        assertEquals(Exceptions.INVALID_EMAIL_FORMAT, ex.getReason());
    }
}
