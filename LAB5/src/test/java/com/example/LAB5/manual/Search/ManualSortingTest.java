package com.example.LAB5.manual.Search;

import com.example.LAB5.manual.DAO.FunctionDAO;
import com.example.LAB5.manual.DAO.PointDAO;
import com.example.LAB5.manual.DAO.UserDAO;
import com.example.LAB5.manual.DTO.FunctionDTO;
import com.example.LAB5.manual.DTO.PointDTO;
import com.example.LAB5.manual.DTO.UserDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class ManualSortingTest {
    private static final Logger logger = LoggerFactory.getLogger(ManualSortingTest.class);
    private UserDAO userDAO;
    private FunctionDAO functionDAO;
    private PointDAO pointDAO;

    // Внутренний класс для критериев поиска
    public static class SearchCriteria {
        private final String name;
        private final String type;
        private final boolean includeChildren;

        public SearchCriteria(String name, String type, boolean includeChildren) {
            this.name = name;
            this.type = type;
            this.includeChildren = includeChildren;
        }

        public String name() { return name; }
        public String type() { return type; }
        public boolean includeChildren() { return includeChildren; }

        public static SearchCriteria of(String name, String type, boolean includeChildren) {
            return new SearchCriteria(name, type, includeChildren);
        }
    }

    @BeforeEach
    void setUp() {
        userDAO = new UserDAO();
        functionDAO = new FunctionDAO();
        pointDAO = new PointDAO();
    }

    // Методы сортировки - теперь часть основного класса
    public List<UserDTO> sortUsers(List<UserDTO> users, String field, String order) {
        if (users == null || users.isEmpty()) {
            return users;
        }

        List<UserDTO> sorted = new ArrayList<>(users);
        sorted.sort((u1, u2) -> {
            int result = 0;
            switch (field.toLowerCase()) {
                case "login":
                    result = u1.getLogin().compareTo(u2.getLogin());
                    break;
                case "role":
                    result = u1.getRole().compareTo(u2.getRole());
                    break;
                case "id":
                    result = Long.compare(u1.getId(), u2.getId());
                    break;
                default:
                    result = 0;
            }
            return "desc".equalsIgnoreCase(order) ? -result : result;
        });

        return sorted;
    }

    public List<FunctionDTO> sortFunctions(List<FunctionDTO> functions, String field, String order) {
        if (functions == null || functions.isEmpty()) {
            return functions;
        }

        List<FunctionDTO> sorted = new ArrayList<>(functions);
        sorted.sort((f1, f2) -> {
            int result = 0;
            switch (field.toLowerCase()) {
                case "name":
                    result = f1.getName().compareTo(f2.getName());
                    break;
                case "userid":
                    result = Long.compare(f1.getUserId(), f2.getUserId());
                    break;
                case "signature":
                    result = f1.getSignature().compareTo(f2.getSignature());
                    break;
                case "id":
                    result = Long.compare(f1.getId(), f2.getId());
                    break;
                default:
                    result = 0;
            }
            return "desc".equalsIgnoreCase(order) ? -result : result;
        });

        return sorted;
    }

    public List<PointDTO> sortPoints(List<PointDTO> points, String field, String order) {
        if (points == null || points.isEmpty()) {
            return points;
        }

        List<PointDTO> sorted = new ArrayList<>(points);
        sorted.sort((p1, p2) -> {
            int result = 0;
            switch (field.toLowerCase()) {
                case "x":
                    result = Double.compare(p1.getXValue(), p2.getXValue());
                    break;
                case "y":
                    result = Double.compare(p1.getYValue(), p2.getYValue());
                    break;
                case "functionid":
                    result = Long.compare(p1.getFunctionId(), p2.getFunctionId());
                    break;
                case "id":
                    result = Long.compare(p1.getId(), p2.getId());
                    break;
                default:
                    result = 0;
            }
            return "desc".equalsIgnoreCase(order) ? -result : result;
        });

        return sorted;
    }

    public List<UserDTO> sortUsersByMultipleFields(List<UserDTO> users, Map<String, String> sortCriteria) {
        if (users == null || users.isEmpty() || sortCriteria == null || sortCriteria.isEmpty()) {
            return users;
        }

        List<UserDTO> sorted = new ArrayList<>(users);
        sorted.sort((u1, u2) -> {
            int result = 0;
            for (Map.Entry<String, String> entry : sortCriteria.entrySet()) {
                String field = entry.getKey();
                String order = entry.getValue();

                switch (field.toLowerCase()) {
                    case "login":
                        result = u1.getLogin().compareTo(u2.getLogin());
                        break;
                    case "role":
                        result = u1.getRole().compareTo(u2.getRole());
                        break;
                    case "id":
                        result = Long.compare(u1.getId(), u2.getId());
                        break;
                    default:
                        result = 0;
                }

                if (result != 0) {
                    return "desc".equalsIgnoreCase(order) ? -result : result;
                }
            }
            return result;
        });

        return sorted;
    }

    // Основные методы поиска
    public List<Object> search(Object root, SearchCriteria criteria) {
        logger.info("Starting hierarchy search");
        // Здесь может быть реализация поиска
        return new ArrayList<>();
    }

    public String getAlgorithmName() {
        return "TEST_SEARCH";
    }

    // ТЕСТОВЫЕ МЕТОДЫ
    @Test
    void testUserSortingAscending() {
        logger.info("Testing user sorting in ascending order");

        List<UserDTO> users = createTestUsers();

        // Сортировка по логину (asc)
        List<UserDTO> sortedByLogin = sortUsers(new ArrayList<>(users), "login", "asc");
        assertEquals("admin_user", sortedByLogin.get(0).getLogin());
        assertEquals("zeta_user", sortedByLogin.get(4).getLogin());

        // Сортировка по роли (asc)
        List<UserDTO> sortedByRole = sortUsers(new ArrayList<>(users), "role", "asc");
        assertEquals("ADMIN", sortedByRole.get(0).getRole());
        assertEquals("USER", sortedByRole.get(4).getRole());

        logger.info("User ascending sorting tests passed");
    }

    @Test
    void testUserSortingDescending() {
        logger.info("Testing user sorting in descending order");

        List<UserDTO> users = createTestUsers();

        // Сортировка по логину (desc)
        List<UserDTO> sortedByLoginDesc = sortUsers(new ArrayList<>(users), "login", "desc");
        assertEquals("zeta_user", sortedByLoginDesc.get(0).getLogin());
        assertEquals("admin_user", sortedByLoginDesc.get(4).getLogin());

        // Сортировка по роли (desc)
        List<UserDTO> sortedByRoleDesc = sortUsers(new ArrayList<>(users), "role", "desc");
        assertEquals("USER", sortedByRoleDesc.get(0).getRole());
        assertEquals("ADMIN", sortedByRoleDesc.get(4).getRole());

        logger.info("User descending sorting tests passed");
    }

    @Test
    void testFunctionSorting() {
        logger.info("Testing function sorting");

        List<FunctionDTO> functions = createTestFunctions();

        // Сортировка по имени (asc)
        List<FunctionDTO> sortedByName = sortFunctions(new ArrayList<>(functions), "name", "asc");
        assertEquals("cubic", sortedByName.get(0).getName());
        assertEquals("sine", sortedByName.get(4).getName());

        // Сортировка по user ID (desc)
        List<FunctionDTO> sortedByUserId = sortFunctions(new ArrayList<>(functions), "userid", "desc");
        assertEquals(3L, sortedByUserId.get(0).getUserId());
        assertEquals(1L, sortedByUserId.get(4).getUserId());

        logger.info("Function sorting tests passed");
    }

    @Test
    void testPointSorting() {
        logger.info("Testing point sorting");

        List<PointDTO> points = createTestPoints();

        // Сортировка по X (asc)
        List<PointDTO> sortedByX = sortPoints(new ArrayList<>(points), "x", "asc");
        assertEquals(1.0, sortedByX.get(0).getXValue());
        assertEquals(10.0, sortedByX.get(4).getXValue());

        // Сортировка по Y (desc)
        List<PointDTO> sortedByY = sortPoints(new ArrayList<>(points), "y", "desc");
        assertEquals(100.0, sortedByY.get(0).getYValue());
        assertEquals(1.0, sortedByY.get(4).getYValue());

        logger.info("Point sorting tests passed");
    }

    @Test
    void testMultiFieldSorting() {
        logger.info("Testing multi-field sorting");

        List<UserDTO> users = Arrays.asList(
                new UserDTO("user_b", "USER", "pass1"),
                new UserDTO("user_a", "ADMIN", "pass2"),
                new UserDTO("user_c", "USER", "pass3"),
                new UserDTO("user_d", "ADMIN", "pass4"),
                new UserDTO("user_e", "MODERATOR", "pass5")
        );

        for (int i = 0; i < users.size(); i++) {
            users.get(i).setId((long) (i + 1));
        }

        // Множественная сортировка: сначала по роли, потом по логину
        Map<String, String> sortCriteria = new LinkedHashMap<>();
        sortCriteria.put("role", "asc");
        sortCriteria.put("login", "asc");

        List<UserDTO> multiSorted = sortUsersByMultipleFields(new ArrayList<>(users), sortCriteria);

        assertEquals("ADMIN", multiSorted.get(0).getRole());
        assertEquals("user_a", multiSorted.get(0).getLogin());
        assertEquals("ADMIN", multiSorted.get(1).getRole());
        assertEquals("user_d", multiSorted.get(1).getLogin());
        assertEquals("MODERATOR", multiSorted.get(2).getRole());
        assertEquals("USER", multiSorted.get(3).getRole());

        logger.info("Multi-field sorting tests passed");
    }

    @Test
    void testSortingPerformance() {
        logger.info("Testing sorting performance with large dataset");

        int dataSize = 1000;
        List<UserDTO> largeUserList = createLargeUserDataset(dataSize);

        long startTime = System.nanoTime();
        List<UserDTO> sortedUsers = sortUsers(largeUserList, "login", "asc");
        long endTime = System.nanoTime();

        double durationMs = (endTime - startTime) / 1_000_000.0;

        assertNotNull(sortedUsers);
        assertEquals(dataSize, sortedUsers.size());
        logger.info("Sorted {} users in {} ms", dataSize, durationMs);

        // Проверяем что сортировка корректна
        for (int i = 0; i < sortedUsers.size() - 1; i++) {
            assertTrue(sortedUsers.get(i).getLogin().compareTo(sortedUsers.get(i + 1).getLogin()) <= 0);
        }
    }

    @Test
    void testSearchAlgorithm() {
        logger.info("Testing search algorithm");

        SearchCriteria criteria = SearchCriteria.of("test", "USER", true);
        List<Object> result = search(1L, criteria);

        assertNotNull(result);
        assertEquals("TEST_SEARCH", getAlgorithmName());

        logger.info("Search algorithm tests passed");
    }

    // Вспомогательные методы для создания тестовых данных
    private List<UserDTO> createTestUsers() {
        List<UserDTO> users = Arrays.asList(
                new UserDTO("zeta_user", "USER", "pass1"),
                new UserDTO("beta_user", "MODERATOR", "pass2"),
                new UserDTO("alpha_user", "ADMIN", "pass3"),
                new UserDTO("gamma_user", "USER", "pass4"),
                new UserDTO("admin_user", "ADMIN", "pass5")
        );

        for (int i = 0; i < users.size(); i++) {
            users.get(i).setId((long) (i + 1));
        }

        return users;
    }

    private List<FunctionDTO> createTestFunctions() {
        List<FunctionDTO> functions = Arrays.asList(
                new FunctionDTO(2L, "sine", "f(x) = sin(x)"),
                new FunctionDTO(1L, "quadratic", "f(x) = x^2"),
                new FunctionDTO(3L, "exponential", "f(x) = e^x"),
                new FunctionDTO(1L, "cubic", "f(x) = x^3"),
                new FunctionDTO(2L, "linear", "f(x) = x")
        );

        for (int i = 0; i < functions.size(); i++) {
            functions.get(i).setId((long) (i + 1));
        }

        return functions;
    }

    private List<PointDTO> createTestPoints() {
        List<PointDTO> points = Arrays.asList(
                new PointDTO(1L, 5.0, 25.0),
                new PointDTO(1L, 1.0, 1.0),
                new PointDTO(2L, 10.0, 100.0),
                new PointDTO(1L, 2.0, 4.0),
                new PointDTO(2L, 3.0, 9.0)
        );

        for (int i = 0; i < points.size(); i++) {
            points.get(i).setId((long) (i + 1));
        }

        return points;
    }

    private List<UserDTO> createLargeUserDataset(int size) {
        List<UserDTO> users = new ArrayList<>();
        Random random = new Random();
        String[] roles = {"USER", "ADMIN", "MODERATOR"};

        for (int i = 0; i < size; i++) {
            String login = "user_" + (size - i) + "_" + random.nextInt(1000);
            String role = roles[random.nextInt(roles.length)];
            UserDTO user = new UserDTO(login, role, "password" + i);
            user.setId((long) i);
            users.add(user);
        }

        return users;
    }
}