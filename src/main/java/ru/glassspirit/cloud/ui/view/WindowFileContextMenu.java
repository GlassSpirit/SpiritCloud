package ru.glassspirit.cloud.ui.view;

import com.vaadin.server.ExternalResource;
import com.vaadin.server.FileDownloader;
import com.vaadin.ui.Button;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import org.vaadin.dialogs.ConfirmDialog;
import ru.glassspirit.cloud.model.SavedFile;
import ru.glassspirit.cloud.ui.view.MainView;

public class WindowFileContextMenu extends Window {
    private MainView mainView;
    private SavedFile file;
    private Button btnOpenDirectory;
    private Button btnDownload;
    private FileDownloader btnDownloadDownloader;
    private Button btnDelete;
    private Button btnRename;

    public WindowFileContextMenu(MainView ui) {
        this.mainView = ui;
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
                mainView.currentDir = mainView.currentDir.resolve(file.getFileName());
                mainView.updateFileGrid();
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
                    mainView.updateFileGrid();
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
        this.btnDownloadDownloader.setFileDownloadResource(
                new ExternalResource(getUI().getPage().getLocation() + mainView.filesService.createDownloadURL(file.getFile())));
    }
}
