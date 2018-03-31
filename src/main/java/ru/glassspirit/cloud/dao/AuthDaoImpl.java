package ru.glassspirit.cloud.dao;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthDaoImpl implements AuthDao {

    private static final Map<String, String> database = new HashMap<>();

    static {
        database.put("user", "pass");
        database.put("test", "test");
    }

    @Override
    public boolean login(String user, String password) {
        return database.containsKey(user) && database.get(user).equals(password);
    }
}
