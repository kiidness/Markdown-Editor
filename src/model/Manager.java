package model;

import model.utils.FileLoader;
import model.utils.StyleLoader;
import model.utils.converters.MarkDownToHtmlConverter;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.*;
import java.nio.file.InvalidPathException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Manager {
    private PropertyChangeSupport support = new PropertyChangeSupport(this);

    public final String PROP_SELECTED_STYLE = "selectedStyle";
    private String selectedStyle;
    public void setSelectedStyle (String selectedStyle) {
        var oldValue = this.selectedStyle;
        this.selectedStyle = selectedStyle;
        MarkDownToHtmlConverter.setCurrentStyle(stylesLoaded.get(selectedStyle));
        for(var fileLoaded: fileLoadeds) { fileLoaded.updateHtmlText(); }
        support.firePropertyChange(PROP_SELECTED_STYLE, oldValue, selectedStyle);
    }

    public final String PROP_STYLES_LOADED = "stylesLoaded";
    private Map<String, String> stylesLoaded = new HashMap<String,String>();
    private void addStyleLoaded(String styleName, String styleText) {
        stylesLoaded.put(styleName, styleText);
        var index = stylesLoaded.size() - 1;
        support.fireIndexedPropertyChange(PROP_STYLES_LOADED, index, null, styleName);
    }

    public final String PROP_FILES_LOADED = "fileLoadeds";
    private List<FileLoaded> fileLoadeds = new ArrayList<>();
    private void addFileLoaded(FileLoaded fileLoaded) {
        fileLoadeds.add(fileLoaded);
        var index = fileLoadeds.indexOf(fileLoaded);
        support.fireIndexedPropertyChange(PROP_FILES_LOADED, index, null, fileLoaded);
    }

    public void closeFileLoaded(FileLoaded fileLoaded)  {
        var index = fileLoadeds.indexOf(fileLoaded);
        fileLoadeds.remove(index);
        if (fileLoadeds.size() == 0) {
            newFile();
        }
        support.fireIndexedPropertyChange(PROP_FILES_LOADED, index, fileLoaded, null);
    }

    public Manager() {

    }

    public void loadAllStyles() throws IOException {
        var styles = StyleLoader.getAllStyles();
        var keySet = styles.keySet();
        for (var styleName : keySet) {
            var text = styles.get(styleName);
            addStyleLoaded(styleName, text);
        }
        setSelectedStyle("Default");
    }

    public void openFile(File file) throws IOException {
        FileLoaded fileLoaded = FileLoader.loadFile(file);
        addFileLoaded(fileLoaded);
    }

    public void newFile () {
        var newFile = new FileLoaded("new File");
        addFileLoaded(newFile);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public void save(FileLoaded fileLoaded) throws IOException, InvalidPathException {
        if (fileLoaded.getFilePath().isEmpty()) {
            throw new InvalidPathException(fileLoaded.getFileName(), "FileLoaded path is null");
        }
        FileLoader.saveFile(fileLoaded);
        fileLoaded.updateSavedText();
    }

    public void saveAs(FileLoaded fileLoaded, File file) throws IOException {
        fileLoaded.setFileName(file.getName());
        fileLoaded.setFilePath(file.getPath());
        try {
            save(fileLoaded);
        } catch (IOException e) {
            fileLoaded.setFileName("new file");
            fileLoaded.setFilePath("");
        }

    }

    public void exportAs(FileLoaded fileLoaded, File file) throws IOException {
        try {
            if (file.getPath().isEmpty()) {
                throw new InvalidPathException(fileLoaded.getFileName(), "FileLoaded path is null");
            }
            FileLoader.exportFile(fileLoaded, file.getPath());
        } catch (IOException e) {
            fileLoaded.setFileName("new file");
            fileLoaded.setFilePath("");
        }
    }
}
