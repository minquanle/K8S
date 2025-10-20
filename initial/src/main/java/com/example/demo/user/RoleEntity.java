// src/main/java/com/example/demo/user/RoleEntity.java
package com.example.demo.user;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "roles")
public class RoleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name; // e.g. ROLE_ADMIN, ROLE_USER

    public RoleEntity() {}
    public RoleEntity(String name) { this.name = name; }

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RoleEntity)) return false;
        RoleEntity that = (RoleEntity) o;
        return Objects.equals(name, that.name);
    }
    @Override public int hashCode() { return Objects.hash(name); }
}
