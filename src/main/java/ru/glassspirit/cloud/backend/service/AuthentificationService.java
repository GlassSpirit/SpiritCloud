package ru.glassspirit.cloud.backend.service;

public interface AuthentificationService {

    boolean login(String login, String password);

    void logout(String user);
}
