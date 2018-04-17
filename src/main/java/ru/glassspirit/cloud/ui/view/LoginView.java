package ru.glassspirit.cloud.ui.view;

import com.vaadin.navigator.View;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.spring.navigator.SpringNavigator;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Label;
import com.vaadin.ui.LoginForm;
import com.vaadin.ui.VerticalLayout;
import org.springframework.beans.factory.annotation.Autowired;
import ru.glassspirit.cloud.service.AuthenticationService;

import javax.annotation.PostConstruct;

@SpringView(name = "login")
public class LoginView extends VerticalLayout implements View {

    //Сервис для работы с авторизацией
    @Autowired
    protected AuthenticationService authenticationService;

    @Autowired
    SpringNavigator navigator;

    //Авторизация
    private LoginForm loginForm;

    @PostConstruct
    void init() {
        loginForm = new LoginForm();
        loginForm.setUsernameCaption("Логин");
        loginForm.setPasswordCaption("Пароль");
        loginForm.setLoginButtonCaption("Авторизоваться");
        Label labelLoginInfo = new Label("");
        loginForm.addLoginListener((loginEvent) -> {
            String login = loginEvent.getLoginParameter("username");
            String password = loginEvent.getLoginParameter("password");

            if (authenticationService.login(login, password)) {
                navigator.navigateTo("");
            }
        });

        this.addComponent(loginForm);
        this.setComponentAlignment(loginForm, Alignment.MIDDLE_CENTER);
    }
}
