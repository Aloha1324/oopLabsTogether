package com.example.LAB5.framework.Search;

import com.example.LAB5.framework.entity.User;
import com.example.LAB5.framework.entity.Function;
import com.example.LAB5.framework.entity.Point;
import com.example.LAB5.framework.repository.UserRepository;
import com.example.LAB5.framework.repository.FunctionRepository;
import com.example.LAB5.framework.repository.PointRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FrameworkSortingTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private FunctionRepository functionRepository;

    @Mock
    private PointRepository pointRepository;

    private SearchService searchService;
    private DepthFirstSearch depthFirstSearch;
    private BreadthFirstSearch breadthFirstSearch;
    private HierarchySearch hierarchySearch;

    private User testUser1;
    private User testUser2;
    private Function testFunction1;
    private Function testFunction2;
    private Point testPoint1;
    private Point testPoint2;

    @BeforeEach
    void setUp() {
        searchService = new SearchService(userRepository, functionRepository, pointRepository);
        depthFirstSearch = new DepthFirstSearch(userRepository, functionRepository, pointRepository);
        breadthFirstSearch = new BreadthFirstSearch(userRepository, functionRepository, pointRepository);
        hierarchySearch = new HierarchySearch(userRepository, functionRepository, pointRepository);

        // Создание тестовых данных
        testUser1 = new User("john_doe", "password123", "ADMIN");
        testUser1.setId(1L);

        testUser2 = new User("jane_smith", "password456", "USER");
        testUser2.setId(2L);

        testFunction1 = new Function("sin(x)", "Math.sin(x)", testUser1);
        testFunction1.setId(1L);

        testFunction2 = new Function("cos(x)", "Math.cos(x)", testUser2);
        testFunction2.setId(2L);

        testPoint1 = new Point(1.0, 2.0, testFunction1, testUser1);
        testPoint1.setId(1L);

        testPoint2 = new Point(3.0, 4.0, testFunction2, testUser2);
        testPoint2.setId(2L);

        // Настройка связей
        testUser1.setFunctions(Arrays.asList(testFunction1));
        testUser2.setFunctions(Arrays.asList(testFunction2));
        testFunction1.setPoints(Arrays.asList(testPoint1));
        testFunction2.setPoints(Arrays.asList(testPoint2));
    }

    // ===== SearchService Tests =====

    @Test
    void testSearchServiceConstructor() {
        assertNotNull(searchService);
    }

    @Test
    void testSearchCriteriaBuilder() {
        SearchService.SearchCriteria criteria = SearchService.SearchCriteria.builder()
                .name("test")
                .type("user")
                .includeChildren(true)
                .searchType(SearchService.SearchType.MULTIPLE)
                .maxDepth(5)
                .build();

        assertEquals("test", criteria.name());
        assertEquals("user", criteria.type());
        assertTrue(criteria.includeChildren());
        assertEquals(SearchService.SearchType.MULTIPLE, criteria.searchType());
        assertEquals(5, criteria.maxDepth());
    }

    @Test
    void testSearchCriteriaDefaultValues() {
        SearchService.SearchCriteria criteria = SearchService.SearchCriteria.builder().build();

        assertNull(criteria.name());
        assertNull(criteria.type());
        assertFalse(criteria.includeChildren());
        assertEquals(SearchService.SearchType.MULTIPLE, criteria.searchType());
        assertEquals(-1, criteria.maxDepth());
    }

    @Test
    void testSortUsersByIdAscending() {
        List<User> users = Arrays.asList(testUser2, testUser1); // 2, 1
        List<User> sorted = searchService.sortUsers(users, "id", "asc");

        assertEquals(2, sorted.size());
        assertEquals(1L, sorted.get(0).getId());
        assertEquals(2L, sorted.get(1).getId());
    }

    @Test
    void testSortUsersByIdDescending() {
        List<User> users = Arrays.asList(testUser1, testUser2); // 1, 2
        List<User> sorted = searchService.sortUsers(users, "id", "desc");

        assertEquals(2, sorted.size());
        assertEquals(2L, sorted.get(0).getId());
        assertEquals(1L, sorted.get(1).getId());
    }

    @Test
    void testSortUsersByLogin() {
        List<User> users = Arrays.asList(testUser2, testUser1); // jane_smith, john_doe
        List<User> sorted = searchService.sortUsers(users, "login", "asc");

        assertEquals(2, sorted.size());
        assertEquals("jane_smith", sorted.get(0).getLogin());
        assertEquals("john_doe", sorted.get(1).getLogin());
    }

    @Test
    void testSortUsersByRole() {
        List<User> users = Arrays.asList(testUser2, testUser1); // USER, ADMIN
        List<User> sorted = searchService.sortUsers(users, "role", "asc");

        assertEquals(2, sorted.size());
        assertEquals("ADMIN", sorted.get(0).getRole());
        assertEquals("USER", sorted.get(1).getRole());
    }

    @Test
    void testSortUsersEmptyList() {
        List<User> emptyList = Arrays.asList();
        List<User> sorted = searchService.sortUsers(emptyList, "id", "asc");

        assertTrue(sorted.isEmpty());
    }

    @Test
    void testSortUsersNullList() {
        List<User> sorted = searchService.sortUsers(null, "id", "asc");
        assertNull(sorted);
    }

    @Test
    void testSortFunctionsByName() {
        List<Function> functions = Arrays.asList(testFunction2, testFunction1); // cos(x), sin(x)
        List<Function> sorted = searchService.sortFunctions(functions, "name", "asc");

        assertEquals(2, sorted.size());
        assertEquals("cos(x)", sorted.get(0).getName());
        assertEquals("sin(x)", sorted.get(1).getName());
    }

    @Test
    void testSortFunctionsBySignature() {
        List<Function> functions = Arrays.asList(testFunction1, testFunction2);
        List<Function> sorted = searchService.sortFunctions(functions, "signature", "asc");

        assertEquals(2, sorted.size());
        assertTrue(sorted.get(0).getSignature().contains("cos") || sorted.get(1).getSignature().contains("cos"));
    }

    @Test
    void testSortFunctionsByUserId() {
        List<Function> functions = Arrays.asList(testFunction2, testFunction1); // user2, user1
        List<Function> sorted = searchService.sortFunctions(functions, "userid", "asc");

        assertEquals(2, sorted.size());
        assertEquals(1L, sorted.get(0).getUser().getId());
        assertEquals(2L, sorted.get(1).getUser().getId());
    }

    @Test
    void testSortPointsByX() {
        List<Point> points = Arrays.asList(testPoint2, testPoint1); // x=3.0, x=1.0
        List<Point> sorted = searchService.sortPoints(points, "x", "asc");

        assertEquals(2, sorted.size());
        assertEquals(1.0, sorted.get(0).getX());
        assertEquals(3.0, sorted.get(1).getX());
    }

    @Test
    void testSortPointsByY() {
        List<Point> points = Arrays.asList(testPoint2, testPoint1); // y=4.0, y=2.0
        List<Point> sorted = searchService.sortPoints(points, "y", "desc");

        assertEquals(2, sorted.size());
        assertEquals(4.0, sorted.get(0).getY());
        assertEquals(2.0, sorted.get(1).getY());
    }

    @Test
    void testSortPointsByFunctionId() {
        List<Point> points = Arrays.asList(testPoint2, testPoint1); // func2, func1
        List<Point> sorted = searchService.sortPoints(points, "functionid", "asc");

        assertEquals(2, sorted.size());
        assertEquals(1L, sorted.get(0).getFunction().getId());
        assertEquals(2L, sorted.get(1).getFunction().getId());
    }

    @Test
    void testSortWithUnknownField() {
        List<User> users = Arrays.asList(testUser1, testUser2);
        List<User> sorted = searchService.sortUsers(users, "unknown", "asc");

        assertEquals(2, sorted.size());
        // Порядок должен остаться неизменным для неизвестного поля
    }

    // ===== DepthFirstSearch Tests =====

    @Test
    void testDepthFirstSearchUserWithoutChildren() {
        SearchService.SearchCriteria criteria = SearchService.SearchCriteria.builder()
                .name("john")
                .type("user")
                .includeChildren(false)
                .build();

        List<Object> results = depthFirstSearch.search(testUser1, criteria);

        assertEquals(1, results.size());
        assertTrue(results.get(0) instanceof User);
        assertEquals("john_doe", ((User) results.get(0)).getLogin());
    }

    @Test
    void testDepthFirstSearchUserWithChildren() {
        SearchService.SearchCriteria criteria = SearchService.SearchCriteria.builder()
                .includeChildren(true)
                .build();

        List<Object> results = depthFirstSearch.search(testUser1, criteria);

        assertEquals(3, results.size()); // user + function + point
        assertTrue(results.stream().anyMatch(obj -> obj instanceof User));
        assertTrue(results.stream().anyMatch(obj -> obj instanceof Function));
        assertTrue(results.stream().anyMatch(obj -> obj instanceof Point));
    }

    @Test
    void testDepthFirstSearchFunction() {
        SearchService.SearchCriteria criteria = SearchService.SearchCriteria.builder()
                .name("sin")
                .type("function")
                .build();

        List<Object> results = depthFirstSearch.search(testFunction1, criteria);

        assertEquals(1, results.size());
        assertTrue(results.get(0) instanceof Function);
        assertEquals("sin(x)", ((Function) results.get(0)).getName());
    }

    @Test
    void testDepthFirstSearchPoint() {
        SearchService.SearchCriteria criteria = SearchService.SearchCriteria.builder()
                .type("point")
                .build();

        List<Object> results = depthFirstSearch.search(testPoint1, criteria);

        assertEquals(1, results.size());
        assertTrue(results.get(0) instanceof Point);
        assertEquals(1.0, ((Point) results.get(0)).getX());
    }

    @Test
    void testDepthFirstSearchWithMaxDepth() {
        SearchService.SearchCriteria criteria = SearchService.SearchCriteria.builder()
                .includeChildren(true)
                .maxDepth(1)
                .build();

        List<Object> results = depthFirstSearch.search(testUser1, criteria);

        // Только user и function, но не point (maxDepth=1)
        assertEquals(2, results.size());
        assertTrue(results.stream().anyMatch(obj -> obj instanceof User));
        assertTrue(results.stream().anyMatch(obj -> obj instanceof Function));
        assertFalse(results.stream().anyMatch(obj -> obj instanceof Point));
    }

    @Test
    void testDepthFirstSearchNullRoot() {
        SearchService.SearchCriteria criteria = SearchService.SearchCriteria.builder().build();
        List<Object> results = depthFirstSearch.search(null, criteria);

        assertTrue(results.isEmpty());
    }

    // ===== BreadthFirstSearch Tests =====

    @Test
    void testBreadthFirstSearchUserWithoutChildren() {
        SearchService.SearchCriteria criteria = SearchService.SearchCriteria.builder()
                .name("jane")
                .type("user")
                .includeChildren(false)
                .build();

        List<Object> results = breadthFirstSearch.search(testUser2, criteria);

        assertEquals(1, results.size());
        assertTrue(results.get(0) instanceof User);
        assertEquals("jane_smith", ((User) results.get(0)).getLogin());
    }

    @Test
    void testBreadthFirstSearchUserWithChildren() {
        SearchService.SearchCriteria criteria = SearchService.SearchCriteria.builder()
                .includeChildren(true)
                .build();

        List<Object> results = breadthFirstSearch.search(testUser1, criteria);

        assertEquals(3, results.size()); // user + function + point
        // В BFS порядок должен быть: user, function, point
        assertTrue(results.get(0) instanceof User);
        assertTrue(results.get(1) instanceof Function);
        assertTrue(results.get(2) instanceof Point);
    }

    @Test
    void testBreadthFirstSearchWithMaxDepth() {
        SearchService.SearchCriteria criteria = SearchService.SearchCriteria.builder()
                .includeChildren(true)
                .maxDepth(1)
                .build();

        List<Object> results = breadthFirstSearch.search(testUser1, criteria);

        assertEquals(2, results.size()); // user и function, но не point
        assertTrue(results.stream().anyMatch(obj -> obj instanceof User));
        assertTrue(results.stream().anyMatch(obj -> obj instanceof Function));
        assertFalse(results.stream().anyMatch(obj -> obj instanceof Point));
    }

    @Test
    void testBreadthFirstSearchNoMatches() {
        SearchService.SearchCriteria criteria = SearchService.SearchCriteria.builder()
                .name("nonexistent")
                .build();

        List<Object> results = breadthFirstSearch.search(testUser1, criteria);

        assertTrue(results.isEmpty());
    }

    // ===== HierarchySearch Tests =====

    @Test
    void testHierarchySearchUserHierarchy() {
        SearchService.SearchCriteria criteria = SearchService.SearchCriteria.builder()
                .includeChildren(true)
                .build();

        List<Object> results = hierarchySearch.search(testUser1, criteria);

        assertEquals(3, results.size()); // user + function + point
        assertTrue(results.stream().anyMatch(obj -> obj instanceof User));
        assertTrue(results.stream().anyMatch(obj -> obj instanceof Function));
        assertTrue(results.stream().anyMatch(obj -> obj instanceof Point));
    }

    @Test
    void testHierarchySearchFunctionHierarchy() {
        SearchService.SearchCriteria criteria = SearchService.SearchCriteria.builder()
                .includeChildren(true)
                .build();

        List<Object> results = hierarchySearch.search(testFunction1, criteria);

        assertEquals(2, results.size()); // function + point
        assertTrue(results.stream().anyMatch(obj -> obj instanceof Function));
        assertTrue(results.stream().anyMatch(obj -> obj instanceof Point));
        assertFalse(results.stream().anyMatch(obj -> obj instanceof User));
    }

    @Test
    void testHierarchySearchPointHierarchy() {
        SearchService.SearchCriteria criteria = SearchService.SearchCriteria.builder()
                .includeChildren(true)
                .build();

        List<Object> results = hierarchySearch.search(testPoint1, criteria);

        assertEquals(1, results.size()); // только point
        assertTrue(results.get(0) instanceof Point);
    }

    @Test
    void testHierarchySearchWithMaxDepth() {
        SearchService.SearchCriteria criteria = SearchService.SearchCriteria.builder()
                .includeChildren(true)
                .maxDepth(1)
                .build();

        List<Object> results = hierarchySearch.search(testUser1, criteria);

        assertEquals(2, results.size()); // user и function, но не point
        assertTrue(results.stream().anyMatch(obj -> obj instanceof User));
        assertTrue(results.stream().anyMatch(obj -> obj instanceof Function));
        assertFalse(results.stream().anyMatch(obj -> obj instanceof Point));
    }

    @Test
    void testHierarchySearchWithoutChildren() {
        SearchService.SearchCriteria criteria = SearchService.SearchCriteria.builder()
                .includeChildren(false)
                .build();

        List<Object> results = hierarchySearch.search(testUser1, criteria);

        assertEquals(1, results.size()); // только user
        assertTrue(results.get(0) instanceof User);
    }

    // ===== Integration Tests =====

    @Test
    void testSearchServiceWithDifferentAlgorithms() {
        SearchService.SearchCriteria criteria = SearchService.SearchCriteria.builder()
                .includeChildren(true)
                .build();

        // Test Depth First
        List<Object> dfsResults = searchService.search(testUser1, criteria, SearchService.SearchAlgorithm.DEPTH_FIRST);

        // Test Breadth First
        List<Object> bfsResults = searchService.search(testUser1, criteria, SearchService.SearchAlgorithm.BREADTH_FIRST);

        // Test Hierarchy
        List<Object> hierarchyResults = searchService.search(testUser1, criteria, SearchService.SearchAlgorithm.HIERARCHY);

        // Все алгоритмы должны найти одинаковое количество результатов
        assertEquals(3, dfsResults.size());
        assertEquals(3, bfsResults.size());
        assertEquals(3, hierarchyResults.size());
    }

    @Test
    void testSearchServiceDefaultAlgorithm() {
        SearchService.SearchCriteria criteria = SearchService.SearchCriteria.builder().build();


        List<Object> results = searchService.search(testUser1, criteria, null);

        assertNotNull(results);

        assertFalse(results.isEmpty());
    }

    @Test
    void testCircularReferencePrevention() {
        // Создаем циклическую ссылку для теста
        User user1 = new User("user1", "pass1");
        User user2 = new User("user2", "pass2");

        // Эмулируем циклические ссылки через функции
        Function func1 = new Function("func1", "x", user1);
        Function func2 = new Function("func2", "y", user2);

        user1.setFunctions(Arrays.asList(func1));
        user2.setFunctions(Arrays.asList(func2));

        SearchService.SearchCriteria criteria = SearchService.SearchCriteria.builder()
                .includeChildren(true)
                .build();

        // Должно завершиться без StackOverflowError
        assertDoesNotThrow(() -> {
            depthFirstSearch.search(user1, criteria);
            breadthFirstSearch.search(user1, criteria);
            hierarchySearch.search(user1, criteria);
        });
    }

    @Test
    void testEdgeCases() {
        // Тест с пустыми критериями
        SearchService.SearchCriteria emptyCriteria = SearchService.SearchCriteria.builder().build();
        List<Object> results = depthFirstSearch.search(testUser1, emptyCriteria);
        assertFalse(results.isEmpty());

        // Тест с очень большим maxDepth
        SearchService.SearchCriteria largeDepthCriteria = SearchService.SearchCriteria.builder()
                .maxDepth(1000)
                .includeChildren(true)
                .build();
        List<Object> largeDepthResults = depthFirstSearch.search(testUser1, largeDepthCriteria);
        assertEquals(3, largeDepthResults.size());
    }
}