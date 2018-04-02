package ru.glassspirit.cloud.ui;

import com.vaadin.annotations.PreserveOnRefresh;
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
import java.nio.file.Paths;

@SpringUI
@PreserveOnRefresh
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

    //Авторизация
    private Panel pnlAuthentification;
    private LoginForm loginForm;

    //Заготовка контекстного меню файла
    private WindowFileContext windowFileContext;

    @Override
    protected void init(VaadinRequest request) {
        rootDir = Paths.get("");
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
            }
        });

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
            if (event.getAllSelectedItems().size() > 1) {
                if (windowFileContext.isAttached()) windowFileContext.close();
            }
            updateButtonStates();
        });
        gridFiles.addItemClickListener(event -> {
            if (windowFileContext.isAttached()) windowFileContext.close();
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
        windowFileContext = new WindowFileContext();
        gridFiles.addContextClickListener(event -> {
            Grid.GridContextClickEvent<SavedFile> gridEvent = (Grid.GridContextClickEvent<SavedFile>) event;
            if (windowFileContext.isAttached()) windowFileContext.close();
            if (gridEvent.getItem() == null) return;
            windowFileContext.setPosition(gridEvent.getClientX(), event.getClientY());
            windowFileContext.setFile(gridEvent.getItem());
            addWindow(windowFileContext);
            windowFileContext.focus();
            gridFiles.deselectAll();
            gridFiles.select(gridEvent.getItem());
        });

        gridFiles.addColumn(SavedFile::getFileName).setCaption("Имя файла");
        gridFiles.addColumn(SavedFile::getFileSize).setCaption("Размер");

        //Нижний тулбар
        HorizontalLayout layoutActionsBottom = new HorizontalLayout();

        uploadFile = new Upload();
        uploadFile.setButtonCaption("Загрузить");
        uploadFile.setReceiver(new Upload.Receiver() {
            @Override
            public OutputStream receiveUpload(String fileName, String mimeType) {
                try {
                    return filesService.getFileOutputStream(currentDir.resolve(fileName).toString());
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

        layoutActionsBottom.addComponents(uploadFile);

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
        updateFileGrid();
    }

    //Обновляем файлы в сетке и строку текущего пути.
    private void updateFileGrid() {
        gridFiles.setItems(SavedFile.fromFileList(filesService.getFilesInDirectory(currentDir.toString())));
        if (windowFileContext.isAttached()) windowFileContext.close();
        updateButtonStates();
    }

    private void updateButtonStates() {
        labelCurrentDir.setValue("." + File.separator + currentDir.toString());
        itemDeleteFile.setEnabled(gridFiles.getSelectedItems().size() > 0);
        itemAscendDir.setEnabled(!currentDir.equals(rootDir));
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

    private class WindowFileContext extends Window {
        private SavedFile file;
        private Button btnOpenDirectory;
        private Button btnDownload;
        private FileDownloader btnDownloadDownloader;
        private Button btnDelete;
        private Button btnRename;

        public WindowFileContext() {
            this.setClosable(false);
            this.setResizable(false);
            this.setDraggable(false);
            VerticalLayout layout = new VerticalLayout();
            layout.setMargin(false);
            layout.setSpacing(false);
            btnOpenDirectory = new NativeButton("Открыть");
            btnOpenDirectory.setSizeFull();
            btnOpenDirectory.addClickListener(event -> {
                if (this.file.getFile().isDirectory()) {
                    currentDir = currentDir.resolve(file.getFileName());
                    updateFileGrid();
                }
                this.close();
            });
            btnDownload = new NativeButton("Скачать");
            btnDownload.setSizeFull();
            btnDownload.addClickListener(event -> {
                //this.close();
            });
            btnDownloadDownloader = new FileDownloader(new ExternalResource(""));
            btnDownloadDownloader.extend(btnDownload);
            btnDelete = new NativeButton("Удалить");
            btnDelete.setSizeFull();
            btnDelete.addClickListener(event -> {
                String caption = file.getFile().isDirectory() ? "Удалить директорию?" : "Удалить файл?";
                ConfirmDialog.show(getUI(), caption, "", "Удалить", "Отмена", dialog -> {
                    if (dialog.isConfirmed()) {
                        this.file.delete();
                        updateFileGrid();
                    }
                });
                this.close();
            });
            btnRename = new NativeButton("Переименовать");
            btnRename.setSizeFull();
            btnRename.addClickListener(event -> {
                this.close();
            });
            layout.addComponents(btnOpenDirectory, btnDownload, btnDelete, btnRename);
            this.setContent(layout);
        }

        public SavedFile getFile() {
            return file;
        }

        public void setFile(SavedFile file) {
            this.file = file;
            this.btnOpenDirectory.setVisible(file.getFile().isDirectory());
            this.btnDownload.setVisible(!file.getFile().isDirectory());
            this.btnDownloadDownloader.setFileDownloadResource(new ExternalResource(createDownloadURL(file)));
        }
    }

}
