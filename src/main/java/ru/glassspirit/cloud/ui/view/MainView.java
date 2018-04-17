package ru.glassspirit.cloud.ui.view;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.navigator.View;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.dialogs.ConfirmDialog;
import ru.glassspirit.cloud.model.SavedFile;
import ru.glassspirit.cloud.service.FilesService;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@SpringView(name = "")
public class MainView extends VerticalLayout implements View {

    //Текущая папка с файлами
    protected Path currentDir;
    //Конечная папка для приложения
    protected Path rootDir;
    //Сервис для работы с файлами
    @Autowired
    FilesService filesService;
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

    //Заготовка контекстного меню файла
    private WindowFileContextMenu windowFileContext;

    @PostConstruct
    void init() {
        rootDir = Paths.get("");
        currentDir = rootDir;

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
            ConfirmDialog.show(UI.getCurrent(), "Удалить файлы?", "", "Удалить", "Отмена", dialog -> {
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
        windowFileContext = new WindowFileContextMenu(this);
        gridFiles.addContextClickListener(event -> {
            Grid.GridContextClickEvent<SavedFile> gridEvent = (Grid.GridContextClickEvent<SavedFile>) event;
            if (windowFileContext.isAttached()) windowFileContext.close();
            if (gridEvent.getItem() == null) return;
            windowFileContext.setPosition(gridEvent.getClientX(), event.getClientY());
            windowFileContext.setFile(gridEvent.getItem());
            UI.getCurrent().addWindow(windowFileContext);
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
        uploadFile.setReceiver((Upload.Receiver) (fileName, mimeType) -> {
            try {
                return filesService.getFileOutputStream(currentDir.resolve(fileName).toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        });
        uploadFile.addFinishedListener(event -> {
            updateFileGrid();
            Notification success = new Notification("Файл " + event.getFilename() + " успешно загружен", Notification.Type.HUMANIZED_MESSAGE);
            success.setDelayMsec(1000);
            success.show(UI.getCurrent().getPage());
        });
        uploadFile.addFailedListener(event -> {
            Notification error = new Notification("Не удалось загрузить файл!", Notification.Type.ERROR_MESSAGE);
            error.setDelayMsec(1000);
            error.show(UI.getCurrent().getPage());
        });

        layoutActionsBottom.addComponents(uploadFile);

        layoutMain.addComponents(layoutActionsTop, gridFiles, layoutActionsBottom);
        pnlMain.setContent(layoutMain);

        //Пока без авторизации
        this.addComponents(pnlMain);
        updateFileGrid();
    }

    //Обновляем файлы в сетке и строку текущего пути.
    protected void updateFileGrid() {
        gridFiles.setItems(SavedFile.fromFileList(filesService.getFilesInDirectory(currentDir.toString())));
        if (windowFileContext.isAttached()) windowFileContext.close();
        updateButtonStates();
    }

    //Обновляем состояния кнопок
    protected void updateButtonStates() {
        labelCurrentDir.setValue("." + File.separator + currentDir.toString());
        itemDeleteFile.setEnabled(gridFiles.getSelectedItems().size() > 0);
        itemAscendDir.setEnabled(!currentDir.equals(rootDir));
    }
}
