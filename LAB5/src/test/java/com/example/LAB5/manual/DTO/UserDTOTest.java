package com.example.LAB5.manual.DTO;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

class UserDTOTest {

    @Test
    @DisplayName("UserDTO - создание с пустыми строками")
    void testUserDTOEmptyStrings() {
        UserDTO user = new UserDTO("", "", "");

        assertEquals("", user.getLogin());
        assertEquals("", user.getRole());
        assertEquals("", user.getPassword());
    }

    @Test
    @DisplayName("UserDTO - создание с ID и проверка геттеров")
    void testUserDTOWithId() {
        UserDTO user = new UserDTO(1L, "testuser", "ADMIN", "password");

        assertEquals(1L, user.getId());
        assertEquals("testuser", user.getLogin());
        assertEquals("ADMIN", user.getRole());
        assertEquals("password", user.getPassword());
    }

    @Test
    @DisplayName("UserDTO - создание без ID")
    void testUserDTOWithoutId() {
        UserDTO user = new UserDTO("testuser", "ADMIN", "password");

        assertNull(user.getId());
        assertEquals("testuser", user.getLogin());
        assertEquals("ADMIN", user.getRole());
        assertEquals("password", user.getPassword());
    }

    @Test
    @DisplayName("UserDTO - проверка формата toString")
    void testUserDTOToStringFormat() {
        UserDTO user = new UserDTO(1L, "testuser", "ADMIN", "password");
        String userString = user.toString();

        assertTrue(userString.startsWith("UserDTO{"));
        assertTrue(userString.contains("id=1"));
        assertTrue(userString.contains("login='testuser'"));
        assertTrue(userString.contains("role='ADMIN'"));
        assertFalse(userString.contains("password="));
        assertTrue(userString.endsWith("}"));
    }

    @Test
    @DisplayName("UserDTO - консистентность hashCode")
    void testUserDTOHashCodeConsistency() {
        UserDTO user = new UserDTO("testuser", "ADMIN", "password");
        user.setId(1L);

        int hashCode1 = user.hashCode();
        int hashCode2 = user.hashCode();

        assertEquals(hashCode1, hashCode2);
    }

    @Test
    @DisplayName("UserDTO - equals с null ID")
    void testUserDTOWithNullIdEquals() {
        UserDTO user1 = new UserDTO("user", "ROLE", "pass");
        UserDTO user2 = new UserDTO("user", "ROLE", "pass");

        // Both have null IDs - should be equal
        assertEquals(user1, user2);

        user1.setId(1L);
        // Now different - should not be equal
        assertNotEquals(user1, user2);
    }

    @Test
    @DisplayName("UserDTO - equals с одинаковыми ID")
    void testUserDTOWithSameIdEquals() {
        UserDTO user1 = new UserDTO(1L, "user", "ROLE", "pass");
        UserDTO user2 = new UserDTO(1L, "user", "ROLE", "pass");

        assertEquals(user1, user2);
        assertEquals(user1.hashCode(), user2.hashCode());
    }

    @Test
    @DisplayName("UserDTO - equals с разными ID")
    void testUserDTOWithDifferentIdEquals() {
        UserDTO user1 = new UserDTO(1L, "user", "ROLE", "pass");
        UserDTO user2 = new UserDTO(2L, "user", "ROLE", "pass");

        assertNotEquals(user1, user2);
    }

    @Test
    @DisplayName("UserDTO - equals с разными логинами")
    void testUserDTOWithDifferentLoginEquals() {
        UserDTO user1 = new UserDTO(1L, "user1", "ROLE", "pass");
        UserDTO user2 = new UserDTO(1L, "user2", "ROLE", "pass");

        assertNotEquals(user1, user2);
    }

    @Test
    @DisplayName("UserDTO - equals с разными ролями")
    void testUserDTOWithDifferentRoleEquals() {
        UserDTO user1 = new UserDTO(1L, "user", "ROLE1", "pass");
        UserDTO user2 = new UserDTO(1L, "user", "ROLE2", "pass");

        assertNotEquals(user1, user2);
    }

    @Test
    @DisplayName("UserDTO - создание с null значениями")
    void testUserDTONullValues() {
        UserDTO user = new UserDTO(null, null, null);

        assertNull(user.getLogin());
        assertNull(user.getRole());
        assertNull(user.getPassword());
        assertNull(user.getId());
    }

    @Test
    @DisplayName("UserDTO - геттеры и сеттеры")
    void testUserDTOGettersAndSetters() {
        UserDTO user = new UserDTO();

        user.setId(1L);
        user.setLogin("testuser");
        user.setRole("USER");
        user.setPassword("secret");

        assertEquals(1L, user.getId());
        assertEquals("testuser", user.getLogin());
        assertEquals("USER", user.getRole());
        assertEquals("secret", user.getPassword());
    }

    @Test
    @DisplayName("UserDTO - equals с null объектом")
    void testUserDTOEqualsWithNull() {
        UserDTO user = new UserDTO(1L, "user", "ROLE", "pass");

        assertNotEquals(user, null);
    }

    @Test
    @DisplayName("UserDTO - equals с объектом другого класса")
    void testUserDTOEqualsWithDifferentClass() {
        UserDTO user = new UserDTO(1L, "user", "ROLE", "pass");

        assertNotEquals(user, "not a user dto");
    }

    @Test
    @DisplayName("UserDTO - equals с самим собой")
    void testUserDTOEqualsWithSelf() {
        UserDTO user = new UserDTO(1L, "user", "ROLE", "pass");

        assertEquals(user, user);
    }

    @Test
    @DisplayName("UserDTO - создание с пробелами в строках")
    void testUserDTOWithSpaces() {
        UserDTO user = new UserDTO("  user  ", "  ROLE  ", "  pass  ");

        assertEquals("  user  ", user.getLogin());
        assertEquals("  ROLE  ", user.getRole());
        assertEquals("  pass  ", user.getPassword());
    }

    @Test
    @DisplayName("UserDTO - создание с длинными строками")
    void testUserDTOWithLongStrings() {
        String longLogin = "a".repeat(1000);
        String longRole = "b".repeat(100);
        String longPassword = "c".repeat(500);

        UserDTO user = new UserDTO(longLogin, longRole, longPassword);

        assertEquals(longLogin, user.getLogin());
        assertEquals(longRole, user.getRole());
        assertEquals(longPassword, user.getPassword());
    }

    @Test
    @DisplayName("UserDTO - создание со специальными символами")
    void testUserDTOWithSpecialCharacters() {
        UserDTO user = new UserDTO("user_123@test.com", "ADMIN_ROLE", "p@ssw0rd!");

        assertEquals("user_123@test.com", user.getLogin());
        assertEquals("ADMIN_ROLE", user.getRole());
        assertEquals("p@ssw0rd!", user.getPassword());
    }

    @Test
    @DisplayName("UserDTO - пароль не влияет на equals и hashCode")
    void testUserDTOPasswordNotAffectingEquals() {
        UserDTO user1 = new UserDTO(1L, "user", "ROLE", "password1");
        UserDTO user2 = new UserDTO(1L, "user", "ROLE", "password2");

        assertEquals(user1, user2);
        assertEquals(user1.hashCode(), user2.hashCode());
    }

    @Test
    @DisplayName("UserDTO - пустой конструктор и поэтапная установка значений")
    void testUserDTOEmptyConstructorAndStepByStepSetters() {
        UserDTO user = new UserDTO();

        assertNull(user.getId());
        assertNull(user.getLogin());
        assertNull(user.getRole());
        assertNull(user.getPassword());

        user.setId(1L);
        assertEquals(1L, user.getId());

        user.setLogin("test");
        assertEquals("test", user.getLogin());

        user.setRole("USER");
        assertEquals("USER", user.getRole());

        user.setPassword("secret");
        assertEquals("secret", user.getPassword());
    }
}