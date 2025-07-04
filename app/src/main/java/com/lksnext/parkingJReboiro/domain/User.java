// app/src/main/java/com/lksnext/parkingplantilla/domain/User.java
package com.lksnext.parkingJReboiro.domain;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String username;
    private String email;
    private String phone;
    private String employeeId;
    private List<String> matriculas;

    public User() {
        this.matriculas = new ArrayList<>();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public List<String> getMatriculas() {
        return matriculas;
    }

    public void setMatriculas(List<String> matriculas) {
        this.matriculas = matriculas != null ? matriculas : new ArrayList<>();
    }
}