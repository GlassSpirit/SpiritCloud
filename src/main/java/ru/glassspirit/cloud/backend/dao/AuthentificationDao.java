package ru.glassspirit.cloud.backend.dao;

import java.util.HashMap;

public class AuthentificationDao {

    public static final HashMap<String, String> database = new HashMap<>();

    static {
        database.put("root", "r0*Tpa$s");
        database.put("test", "password");
        database.put("test1", "test123");
    }


}
