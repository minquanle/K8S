package com.example.demo.user;

import com.example.demo.user.dto.UserDto;
import com.example.demo.user.dto.UserUpsertRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UsersController {

    private final UserRepository users;
    private final RoleRepository roles;
    private final PasswordEncoder encoder;

    public UsersController(UserRepository users, RoleRepository roles, PasswordEncoder encoder) {
        this.users = users;
        this.roles = roles;
        this.encoder = encoder;
    }

    @GetMapping
    public List<UserDto> findAll() {
        return users.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> findOne(@PathVariable Long id) {
        return users.findById(id).map(u -> ResponseEntity.ok(toDto(u))).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody UserUpsertRequest in) {
        if (in.getUsername() == null || in.getUsername().isBlank() || in.getPassword() == null || in.getPassword().isBlank()) {
            return ResponseEntity.badRequest().body("username and password are required");
        }
        if (users.findByUsername(in.getUsername()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("username already exists");
        }
        UserEntity u = new UserEntity();
        u.setUsername(in.getUsername());
        u.setPassword(encoder.encode(in.getPassword()));
        u.setEnabled(in.getEnabled() == null ? true : in.getEnabled());
        if (in.getRoles() != null) {
            in.getRoles().forEach(rn -> u.getRoles().add(resolveRole(rn)));
        }
        UserEntity saved = users.save(u);
        return ResponseEntity.created(URI.create("/api/users/" + saved.getId())).body(toDto(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody UserUpsertRequest in) {
        Optional<UserEntity> opt = users.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        UserEntity u = opt.get();
        if (in.getUsername() != null && !in.getUsername().isBlank() && !in.getUsername().equals(u.getUsername())) {
            if (users.findByUsername(in.getUsername()).isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("username already exists");
            }
            u.setUsername(in.getUsername());
        }
        if (in.getPassword() != null && !in.getPassword().isBlank()) {
            u.setPassword(encoder.encode(in.getPassword()));
        }
        if (in.getEnabled() != null) {
            u.setEnabled(in.getEnabled());
        }
        if (in.getRoles() != null) {
            u.getRoles().clear();
            in.getRoles().forEach(rn -> u.getRoles().add(resolveRole(rn)));
        }
        UserEntity saved = users.save(u);
        return ResponseEntity.ok(toDto(saved));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!users.existsById(id)) return ResponseEntity.notFound().build();
        users.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private RoleEntity resolveRole(String roleName) {
        String normalized = roleName.startsWith("ROLE_") ? roleName : ("ROLE_" + roleName);
        return roles.findByName(normalized).orElseGet(() -> roles.save(new RoleEntity(normalized)));
    }

    private UserDto toDto(UserEntity u) {
        UserDto dto = new UserDto();
        dto.setId(u.getId());
        dto.setUsername(u.getUsername());
        dto.setEnabled(u.isEnabled());
        dto.setRoles(u.getRoles().stream()
                .map(RoleEntity::getName)
                .map(n -> n.replaceFirst("^ROLE_", ""))
                .collect(Collectors.toList()));
        return dto;
    }
}

