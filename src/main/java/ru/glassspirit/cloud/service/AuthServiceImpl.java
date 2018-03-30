package ru.glassspirit.cloud.service;

import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    @Override
    public boolean login(String user, String pass) {
        //Пока пустая авторизация
        return true;
    }

    @Override
    public void logout(String user) {

    }

}
