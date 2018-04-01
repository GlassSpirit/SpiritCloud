package ru.glassspirit.cloud.ui;

import com.vaadin.annotations.Title;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.dialogs.ConfirmDialog;
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
    //Конечная папка для приложения
    private Path rootDir;

    //Основная панель
    private Panel pnlMain;

    //Верхний тулбар
    private MenuBar toolbarTop;
    private MenuBar.MenuItem itemAscendDir;
    private MenuBar.MenuItem itemDeleteFile;
    private MenuBar.MenuItem itemUpdateGrid;
    private Label labelCurrentDir;

    //Сетка файлов
    private Grid<SavedFile> gridFiles;

    //Нижний тулбар
    private Upload uploadFile;
    private Button btnDownloadFile;
    private FileDownloader btnDownloadFileDownloader;

    //Авторизация
    private Panel pnlAuthentification;
    private LoginForm loginForm;

    @Override
    protected void init(VaadinRequest request) {
        rootDir = filesService.getRootPath().relativize(filesService.getRootPath());
        currentDir = rootDir;

        //Основная страница
        VerticalLayout layoutSource = new VerticalLayout();

        //Основная панель
        pnlMain = new Panel();
        VerticalLayout layoutMain = new VerticalLayout();
        layoutMain.setSpacing(false);

        //Верхний тулбар
        HorizontalLayout layoutActionsTop = new HorizontalLayout();

        toolbarTop = new MenuBar();
        toolbarTop.setStyleName("small");

        itemAscendDir = toolbarTop.addItem("Вверх", VaadinIcons.ARROW_UP, menuItem -> {
            if (!currentDir.equals(rootDir)) {
                currentDir = (currentDir.getParent() != null ? currentDir.getParent() : rootDir);
                updateFileGrid();
                itemAscendDir.setEnabled(!currentDir.equals(rootDir));
            }
        });
        itemAscendDir.setEnabled(false);

        itemDeleteFile = toolbarTop.addItem("Удалить", VaadinIcons.TRASH, menuItem -> {
            ConfirmDialog.show(this, "Удалить файлы?", "", "Удалить", "Отмена", dialog -> {
                if (dialog.isConfirmed()) {
                    for (SavedFile file : gridFiles.getSelectedItems()) {
                        file.delete();
                    }
                    updateFileGrid();
                }
            });
        });
        itemDeleteFile.setEnabled(false);

        itemUpdateGrid = toolbarTop.addItem("Обновить", VaadinIcons.REFRESH, menuItem -> {
            updateFileGrid();
        });
        labelCurrentDir = new Label();

        layoutActionsTop.addComponents(toolbarTop, labelCurrentDir);

        //Сетка файлов
        gridFiles = new Grid<>();
        gridFiles.setSizeFull();
        gridFiles.setSelectionMode(Grid.SelectionMode.MULTI);
        gridFiles.addSelectionListener(event -> {
            itemDeleteFile.setEnabled(event.getAllSelectedItems().size() > 0);
            btnDownloadFile.setEnabled(event.getAllSelectedItems().size() == 1
                    && !event.getAllSelectedItems().iterator().next().getFile().isDirectory());
            event.getFirstSelectedItem().ifPresent(firstSelected -> {
                String url = createDownloadURL(firstSelected);
                btnDownloadFileDownloader.setFileDownloadResource(new ExternalResource(url));
            });
        });
        gridFiles.addItemClickListener(event -> {
            if (event.getMouseEventDetails().isDoubleClick() || event.getMouseEventDetails().isShiftKey()) {
                if (event.getItem().getFile().isDirectory() && !event.getMouseEventDetails().isShiftKey()) {
                    currentDir = currentDir.resolve(event.getItem().getFileName());
                    itemAscendDir.setEnabled(!currentDir.equals(rootDir));
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

        //Нижний тулбар
        HorizontalLayout layoutActionsBottom = new HorizontalLayout();

        uploadFile = new Upload();
        uploadFile.setButtonCaption("Загрузить");
        uploadFile.setReceiver(new Upload.Receiver() {
            @Override
            public OutputStream receiveUpload(String fileName, String mimeType) {
                try {
                    return filesService.getFileOutputStream(filesService.getRootPath().resolve(currentDir).resolve(fileName));
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

        btnDownloadFile = new Button("Скачать");
        btnDownloadFile.setEnabled(false);
        btnDownloadFileDownloader = new FileDownloader(new ExternalResource(""));
        btnDownloadFileDownloader.extend(btnDownloadFile);

        layoutActionsBottom.addComponents(uploadFile, btnDownloadFile);

        layoutMain.addComponents(layoutActionsTop, gridFiles, layoutActionsBottom);
        pnlMain.setContent(layoutMain);

        //Панель авторизации
        /*pnlAuthentification = new Panel("Авторизация");
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
        pnlAuthentification.setContent(layoutAuthentification);*/

        //Пока без авторизации
        layoutSource.addComponents(pnlMain/*, pnlAuthentification*/);
        this.setContent(layoutSource);
    }

    //Обновляем файлы в сетке и строку текущего пути.
    private void updateFileGrid() {
        gridFiles.setItems(
                SavedFile.fromFileList(filesService.getFilesInDirectory(filesService.getRootPath().resolve(currentDir))));
        labelCurrentDir.setValue("." + File.separator + currentDir.toString());
    }

    private String createDownloadURL(SavedFile file) {
        StringBuilder builder = new StringBuilder();
        //Добавляем исходный адрес
        builder.append(getPage().getLocation().toString());
        //Запрос на скачивание
        builder.append("/download?file_path=");
        //Указываем путь к файлу относительно rootPath
        builder.append(filesService.getRootPath().relativize(file.getFile().toPath()).toString().replace(File.separator, "/"));
        return builder.toString();
    }

}
