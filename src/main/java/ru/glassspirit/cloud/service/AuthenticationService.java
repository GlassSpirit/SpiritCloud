package ru.glassspirit.cloud.service;

public interface AuthenticationService {

    boolean isAuthenticated();

    boolean login(String login, String password);

    void logout();

}
