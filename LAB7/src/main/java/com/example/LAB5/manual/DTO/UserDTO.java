package com.example.LAB5.manual.DTO;

import java.util.Objects;

public class UserDTO {

    private Long id;
    private String login;
    private String role;
    private String password;

    public UserDTO() {
    }

    public UserDTO(String login, String role, String password) {
        this.login = login;
        this.role = role;
        this.password = password;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // equals/hashCode по id, login и role, без password
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof UserDTO)) {
            return false;
        }
        UserDTO that = (UserDTO) other;
        return Objects.equals(id, that.id)
                && Objects.equals(login, that.login)
                && Objects.equals(role, that.role);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, login, role);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("UserDTO{");
        sb.append("id=").append(id);
        sb.append(", login='").append(login).append('\'');
        sb.append(", role='").append(role).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
