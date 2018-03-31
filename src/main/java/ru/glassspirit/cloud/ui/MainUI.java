package ru.glassspirit.cloud.ui;

import com.vaadin.annotations.Title;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.*;
import org.springframework.beans.factory.annotation.Autowired;
import ru.glassspirit.cloud.model.SavedFile;
import ru.glassspirit.cloud.service.AuthService;
import ru.glassspirit.cloud.service.FilesService;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;

@SpringUI
@Title("SpiritCloud")
public class MainUI extends UI {

    //Сервис для работы с авторизацией
    @Autowired
    AuthService authService;

    //Сервис для работы с файлами
    @Autowired
    FilesService filesService;

    //Текущая папка с файлами
    private Path currentDir;

    //Верхняя панель действий
    private Panel pnlActionsTop;
    private MenuBar menuBar;
    private Label labelCurrentDir;

    //Сетка файлов
    private Grid<SavedFile> gridFiles;

    //Нижняя панель действий
    private Panel pnlActionsBottom;
    private Upload uploadFile;
    private Button btnDelete;
    private Button btnDownload;
    private Button btnUpdate;

    //Авторизация
    private Panel pnlAuthentification;
    private LoginForm loginForm;

    @Override
    protected void init(VaadinRequest request) {
        currentDir = filesService.getRootPath();
        VerticalLayout layoutSource = new VerticalLayout();
        layoutSource.setSpacing(false);

        pnlActionsTop = new Panel();
        HorizontalLayout layoutActionsTop = new HorizontalLayout();
        layoutActionsTop.setSpacing(false);

        menuBar = new MenuBar();
        MenuBar.MenuItem fileItem = menuBar.addItem("Выше", menuItem -> {
            if (!currentDir.equals(filesService.getRootPath())) {
                currentDir = currentDir.getParent();
                updateFileGrid();
            }
        });
        labelCurrentDir = new Label();

        layoutActionsTop.addComponents(menuBar, labelCurrentDir);
        pnlActionsTop.setContent(layoutActionsTop);

        gridFiles = new Grid<>();
        gridFiles.setSizeFull();
        gridFiles.setSelectionMode(Grid.SelectionMode.MULTI);
        gridFiles.addSelectionListener(event -> {
            btnDelete.setEnabled(event.getAllSelectedItems().size() > 0);
            btnDownload.setEnabled(event.getAllSelectedItems().size() == 1);
        });
        gridFiles.addItemClickListener(event -> {
            if (event.getMouseEventDetails().isDoubleClick()) {
                if (event.getItem().getFile().isDirectory()) {
                    currentDir = currentDir.resolve(event.getItem().getFileName());
                    updateFileGrid();
                    return;
                }
                if (event.getColumn() != null) {
                    if (event.getSource().getSelectedItems().contains(event.getItem())) {
                        event.getSource().deselect(event.getItem());
                    } else {
                        event.getSource().select(event.getItem());
                    }
                }
            }
        });
        gridFiles.addColumn(SavedFile::getFileName).setCaption("Имя файла");
        gridFiles.addColumn(SavedFile::getFileSize).setCaption("Размер");
        updateFileGrid();

        //Панель действий
        pnlActionsBottom = new Panel();
        HorizontalLayout layoutActionsBottom = new HorizontalLayout();
        layoutActionsBottom.setMargin(true);
        layoutActionsBottom.setSpacing(true);

        uploadFile = new Upload();
        uploadFile.setButtonCaption("Загрузить");
        uploadFile.setReceiver(new Upload.Receiver() {
            @Override
            public OutputStream receiveUpload(String fileName, String mimeType) {
                try {
                    return filesService.getFileOutputStream(currentDir.resolve(fileName));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        });
        uploadFile.addFinishedListener(event -> {
            updateFileGrid();
            Notification success = new Notification("Файл " + event.getFilename() + " успешно загружен", Notification.Type.HUMANIZED_MESSAGE);
            success.setDelayMsec(1000);
            success.show(getPage());
        });
        uploadFile.addFailedListener(event -> {
            Notification error = new Notification("Не удалось загрузить файл!", Notification.Type.ERROR_MESSAGE);
            error.setDelayMsec(1000);
            error.show(getPage());
        });
        btnDelete = new Button("Удалить");
        btnDelete.setEnabled(false);
        btnDelete.addClickListener(event -> {
           for (SavedFile file : gridFiles.getSelectedItems()) {
               file.getFile().delete();
           }
           updateFileGrid();
        });
        btnDownload = new Button("Скачать");
        btnDownload.setEnabled(false);
        btnDownload.addClickListener(event -> {
            getPage().open(getPage().getLocation().resolve("/files/" + gridFiles.getSelectedItems().iterator().next().getFileName()).toString(), "Download");
        });
        btnUpdate = new Button("Обновить");
        btnUpdate.addClickListener(event -> {
            updateFileGrid();
        });

        layoutActionsBottom.addComponents(uploadFile, btnDelete, btnDownload, btnUpdate);
        pnlActionsBottom.setContent(layoutActionsBottom);

        //Панель авторизации
        pnlAuthentification = new Panel("Авторизация");
        HorizontalLayout layoutAuthentification = new HorizontalLayout();

        loginForm = new LoginForm();
        loginForm.setUsernameCaption("Логин");
        loginForm.setPasswordCaption("Пароль");
        loginForm.setLoginButtonCaption("Авторизоваться");
        Label labelLoginInfo = new Label("");
        loginForm.addLoginListener((loginEvent) -> {
            String login = loginEvent.getLoginParameter("username");
            String password = loginEvent.getLoginParameter("password");
            if (authService.login(login, password)) {
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

        //Пока без авторизации
        layoutSource.addComponents(pnlActionsTop, gridFiles, pnlActionsBottom/*, pnlAuthentification*/);
        this.setContent(layoutSource);
    }

    public void updateFileGrid() {
        gridFiles.setItems(SavedFile.fromFileList(filesService.getFilesInDirectory(currentDir)));
        labelCurrentDir.setValue("root" + File.separator + filesService.getRootPath().relativize(currentDir).normalize().toString());
    }

}
