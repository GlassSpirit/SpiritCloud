package ru.glassspirit.cloud.ui;

import com.vaadin.annotations.Title;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.glassspirit.cloud.service.AuthService;
import ru.glassspirit.cloud.model.SavedFile;

import java.io.OutputStream;

@SpringUI
@Title("SpiritCloud")
public class MainUI extends UI {

    @Autowired
    @Qualifier("frontAuthService")
    AuthService authentification;

    @Override
    protected void init(VaadinRequest request) {
        VerticalLayout layoutSource = new VerticalLayout();

        Grid<SavedFile> gridFiles = new Grid<>();
        gridFiles.setSizeFull();

        //Панель действий
        Panel pnlActions = new Panel();
        HorizontalLayout layoutActions = new HorizontalLayout();

        Upload uploadFile = new Upload();
        uploadFile.setButtonCaption("Загрузить");
        uploadFile.setReceiver(new Upload.Receiver() {
            @Override
            public OutputStream receiveUpload(String fileName, String mimeType) {
                return null;
            }
        });
        Button btnDelete = new Button("Удалить");
        Button btnDownload = new Button("Скачать");

        layoutActions.addComponents(uploadFile, btnDelete, btnDownload);
        pnlActions.setContent(layoutActions);

        //Панель авторизации
        Panel pnlAuthentification = new Panel("Авторизация");
        HorizontalLayout layoutAuthentification = new HorizontalLayout();

        LoginForm loginForm = new LoginForm();
        loginForm.setUsernameCaption("Логин");
        loginForm.setPasswordCaption("Пароль");
        loginForm.setLoginButtonCaption("Авторизоваться");
        Label labelLoginInfo = new Label("");
        loginForm.addLoginListener((loginEvent) -> {
            String login = loginEvent.getLoginParameter("username");
            String password = loginEvent.getLoginParameter("password");
            if (authentification.login(login, password)) {
                labelLoginInfo.setValue("Вход выполнен");
                loginForm.setVisible(false);
                Notification success = new Notification("Добро пожаловать, " + login, Notification.Type.HUMANIZED_MESSAGE);
                success.setDelayMsec(1000);
                success.show(this.getPage());
            } else {
                Notification error = new Notification("Неверный логин или пароль", Notification.Type.ERROR_MESSAGE);
                error.setDelayMsec(1000);
                error.show(this.getPage());
            }
        });

        layoutAuthentification.addComponents(loginForm, labelLoginInfo);
        pnlAuthentification.setContent(layoutAuthentification);

        layoutSource.addComponents(gridFiles, pnlActions, pnlAuthentification);
        this.setContent(layoutSource);
    }

}
