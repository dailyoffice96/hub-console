package com.smconsole.user;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    public ResponseEntity<Page<UserResponse>> getSearch(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) UserStatus status,
            Pageable pageable
    ){ Page<UserResponse> users = userService.getSearch(name, phone, status, pageable);
       return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long LoginId) {
        UserResponse user = userService.getUser(LoginId);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> update(@PathVariable Long id, @RequestBody UserResponse dto){
        UserResponse user = userService.update(id, dto);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}/withdraw")
    public ResponseEntity<UserResponse> withdraw(@PathVariable Long id){
        UserResponse user = userService.withdraw(id);
        return ResponseEntity.ok(user);
    }

}

