package ru.glassspirit.cloud.backend.service;

import org.springframework.stereotype.Service;
import ru.glassspirit.cloud.backend.dao.AuthentificationDao;

@Service
public class Authentification implements AuthentificationService {

    @Override
    public boolean login(String login, String password) {
        return AuthentificationDao.database.containsKey(login) && AuthentificationDao.database.get(login).equals(password);
    }

    @Override
    public void logout(String user) {

    }
}
