package viewmodel;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.FileLoaded;
import model.Manager;

import java.beans.IndexedPropertyChangeEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class ManagerVM implements PropertyChangeListener {
    private Manager model;

    private StringProperty selectedStyle = new SimpleStringProperty();
    public StringProperty seletedStyleProperty() { return selectedStyle; }
    public void setSelectedStyle(String selectedStyle) { this.selectedStyle.setValue(selectedStyle); }

    private ObservableList<String> styleListObs = FXCollections.observableArrayList(new ArrayList<>());
    private ListProperty<String> styleList = new SimpleListProperty<>(styleListObs);
    public ReadOnlyListProperty<String> styleListProperty() { return styleList; }

    private ObservableList<FileLoadedVM> fileLoadedVMSObs = FXCollections.observableArrayList(new ArrayList<>());
    private ListProperty<FileLoadedVM> fileLoadedVMS = new SimpleListProperty<>(fileLoadedVMSObs);
    public ReadOnlyListProperty<FileLoadedVM> fileLoadedVMSProperty() { return fileLoadedVMS; }

    public ManagerVM() {
        model = new Manager();
        model.addPropertyChangeListener(this);
        selectedStyle.addListener((arg, old, newV) -> { model.setSelectedStyle(newV); });
    }

    public void loadAllStyles() throws IOException {
        model.loadAllStyles();
    }

    public void newFile() {
        model.newFile();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(model.PROP_FILES_LOADED)) {
            var indexedEvt = (IndexedPropertyChangeEvent)evt;
            if (indexedEvt.getOldValue() == null && indexedEvt.getNewValue() != null) {
                var fileLoaded = (FileLoaded) indexedEvt.getNewValue();
                var fileLoadedVM = new FileLoadedVM(fileLoaded);
                fileLoadedVMSObs.add(indexedEvt.getIndex(), fileLoadedVM);
            } else if (indexedEvt.getOldValue() != null && indexedEvt.getNewValue() == null) {
                fileLoadedVMSObs.remove(indexedEvt.getIndex());
            }
        } else if (evt.getPropertyName().equals(model.PROP_STYLES_LOADED)) {
            var indexedEvt = (IndexedPropertyChangeEvent)evt;
            if (indexedEvt.getOldValue() == null && indexedEvt.getNewValue() != null) {
                var style = (String) indexedEvt.getNewValue();
                styleListObs.add(indexedEvt.getIndex(), style);
            } else if (indexedEvt.getOldValue() != null && indexedEvt.getNewValue() == null) {
                styleListObs.remove(indexedEvt.getIndex());
            }
        } else if (evt.getPropertyName().equals(model.PROP_SELECTED_STYLE)) {
            if (!evt.getNewValue().equals(evt.getOldValue())) {
                var selectedStyle = (String)evt.getNewValue();
                this.selectedStyle.setValue(selectedStyle);
            }
        }
    }

    public void closeFile(FileLoadedVM fileLoadedVM) {
        model.closeFileLoaded(fileLoadedVM.getModel());
    }

    public void openFile(File file) throws IOException {
        model.openFile(file);
    }

    public void save (FileLoadedVM fileLoadedVM) throws IOException {
        model.save(fileLoadedVM.getModel());
    }

    public void saveAs(FileLoadedVM fileLoadedVM, File file) throws IOException {
        model.saveAs(fileLoadedVM.getModel(), file);
    }

    public void exportAs(FileLoadedVM fileLoadedVM, File file) throws IOException {
        model.exportAs(fileLoadedVM.getModel(), file);
    }
}
