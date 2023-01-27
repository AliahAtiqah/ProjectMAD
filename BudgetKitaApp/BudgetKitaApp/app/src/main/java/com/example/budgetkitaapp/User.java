package com.example.budgetkitaapp;

public class User {

    public String username, email, phone, company;

    public User(){

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

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public User(String username, String email, String phone, String company){
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.company = company;
    }
}
