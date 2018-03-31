package ru.glassspirit.cloud.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.glassspirit.cloud.dao.AuthDao;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    AuthDao authDao;

    @Override
    public boolean login(String user, String pass) {
        return authDao.login(user, pass);
    }

    @Override
    public void logout(String user) {

    }

}
