package view.utils;
import javafx.scene.control.Tab;
import launcher.Main;
import view.FileEditorControl;
import viewmodel.FileLoadedVM;
import viewmodel.ManagerVM;

import java.io.IOException;
import java.nio.file.InvalidPathException;

public abstract class TabFactory {
    public static Tab createTab(ManagerVM managerVM, FileLoadedVM fileLoadedVM) {
        var tab = new Tab();
        tab.textProperty().bind(fileLoadedVM.fileNameProperty());
        var fileEditorControl = new FileEditorControl(managerVM, fileLoadedVM);
        tab.setContent(fileEditorControl);

        tab.setOnCloseRequest(__ -> {
            if (!fileLoadedVM.isFileSaved()) {
                boolean mustSave = Main.openNotSavedFileWindow();
                if (mustSave) {
                    try {
                        managerVM.save(fileLoadedVM);
                    } catch (InvalidPathException e) {
                        var file = Main.openSaveFileDialog();
                        if (file != null) {
                            try {
                                managerVM.saveAs(fileLoadedVM, file);
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                        }
                    } catch (IOException e) {
                        Main.displayError(e);
                    }
                }
            }
            managerVM.closeFile(fileLoadedVM);
        });

        return tab;
    }
}
