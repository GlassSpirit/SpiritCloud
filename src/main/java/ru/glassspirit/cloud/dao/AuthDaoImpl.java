package ru.glassspirit.cloud.dao;

import java.util.HashMap;
import java.util.Map;

public class AuthDaoImpl implements AuthDao {

    private static final Map<String, String> database = new HashMap<>();

    static {
        database.put("user", "pass");
        database.put("test", "test");
    }

    @Override
    public boolean login(String user, String password) {
        return false;
    }
}
