package com.example.demo.user.dto;

import java.util.List;

public class UserUpsertRequest {
    private String username;
    private String password; // optional on update; required on create
    private Boolean enabled; // optional; default true
    private List<String> roles; // e.g. ["ADMIN","USER"]

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }
}

