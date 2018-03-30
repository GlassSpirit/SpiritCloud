package ru.glassspirit.cloud.service;

public interface AuthService {

    boolean login(String user, String pass);

    void logout(String user);

}
