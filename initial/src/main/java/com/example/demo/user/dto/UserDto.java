package com.example.demo.user.dto;

import java.util.List;

public class UserDto {
    private Long id;
    private String username;
    private boolean enabled;
    private List<String> roles; // e.g. ["ADMIN","USER"]

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }
}

