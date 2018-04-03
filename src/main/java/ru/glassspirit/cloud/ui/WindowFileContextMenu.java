package ru.glassspirit.cloud.ui;

import com.vaadin.server.ExternalResource;
import com.vaadin.server.FileDownloader;
import com.vaadin.ui.Button;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import org.vaadin.dialogs.ConfirmDialog;
import ru.glassspirit.cloud.model.SavedFile;

public class WindowFileContextMenu extends Window {
    private MainUI mainUI;
    private SavedFile file;
    private Button btnOpenDirectory;
    private Button btnDownload;
    private FileDownloader btnDownloadDownloader;
    private Button btnDelete;
    private Button btnRename;

    public WindowFileContextMenu(MainUI ui) {
        this.mainUI = ui;
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
                mainUI.currentDir = mainUI.currentDir.resolve(file.getFileName());
                mainUI.updateFileGrid();
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
                    mainUI.updateFileGrid();
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
        this.btnDownloadDownloader.setFileDownloadResource(new ExternalResource(mainUI.createDownloadURL(file)));
    }
}
