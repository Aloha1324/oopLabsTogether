package com.example.LAB5.framework.controller;

import com.example.LAB5.framework.entity.Function;
import com.example.LAB5.framework.entity.User;
import com.example.LAB5.framework.service.FunctionService;
import com.example.LAB5.framework.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/functions")
public class FunctionController {

    private final FunctionService functionService;
    private final UserService userService;

    public FunctionController(FunctionService functionService, UserService userService) {
        this.functionService = functionService;
        this.userService = userService;
    }

    // GET /api/functions
    @GetMapping
    public List<Function> getAllFunctions() {
        return functionService.getAllFunctions();
    }

    // GET /api/functions/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Function> getFunction(@PathVariable Long id) {
        Function function = functionService.getFunctionByIdOrNull(id);
        return function != null ? ResponseEntity.ok(function) : ResponseEntity.notFound().build();
    }

    // GET /api/functions/by-user/{userId}
    @GetMapping("/by-user/{userId}")
    public List<Function> getFunctionsByUser(@PathVariable Long userId) {
        return functionService.getFunctionsByUserId(userId);
    }

    // POST /api/functions
    @PostMapping
    public ResponseEntity<Function> createFunction(@RequestParam Long userId,
                                                   @RequestParam String name,
                                                   @RequestParam String expression) {
        User user = userService.getUserByIdOrNull(userId);
        if (user == null) {
            return ResponseEntity.badRequest().build();
        }
        Function function = functionService.createFunction(user, name, expression);
        return ResponseEntity.ok(function);
    }

    // PUT /api/functions/{id}
    @PutMapping("/{id}")
    public ResponseEntity<Function> updateFunction(@PathVariable Long id,
                                                   @RequestParam String name,
                                                   @RequestParam String expression) {
        Function updated = functionService.updateFunction(id, name, expression);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    // DELETE /api/functions/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFunction(@PathVariable Long id) {
        boolean deleted = functionService.deleteFunction(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}