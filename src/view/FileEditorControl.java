package view;

import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.IndexRange;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebView;
import org.fxmisc.richtext.CodeArea;
import view.utils.CodeAreaInitializer;
import viewmodel.FileLoadedVM;
import viewmodel.ManagerVM;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public class FileEditorControl extends BorderPane {
    private FileLoadedVM fileLoadedVM;
    public FileLoadedVM getViewModel() {
        return fileLoadedVM;
    }

    @FXML
    private CodeArea codeArea;
    @FXML
    private WebView webView;

    // For traduction
    @FXML
    private Label selectedLineAndColLabel;

    public FileEditorControl(ManagerVM managerVM, FileLoadedVM fileLoadedVM) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/fileEditorControl.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        setLocale(Locale.getDefault().toString());

        codeArea.getStylesheets().add(MainApp.class.getResource("/style/codeAreaStyle.css").toExternalForm());

        this.fileLoadedVM = fileLoadedVM;
        initialize();
    }

    private void initialize() {
        webView.getEngine().loadContent(fileLoadedVM.getHtmlText(), "text/html");

        fileLoadedVM.htmlTextProperty().addListener((___) -> {
            webView.getEngine().loadContent(fileLoadedVM.getHtmlText(), "text/html");
        });

        codeArea.textProperty().addListener((___, oldText, newVText) -> {
            fileLoadedVM.markDownTextProperty().setValue(newVText);
        });

        fileLoadedVM.markDownTextProperty().addListener((___, oldText, newVText) -> {
            codeArea.replaceText(fileLoadedVM.getMarkDownText());
        });

        CodeAreaInitializer.initialize(codeArea);

        selectedLineAndColLabel.textProperty().bind(
                Bindings.createStringBinding(() -> MessageFormat.format(
                        "{0} {1}",
                        codeArea.getCaretColumn(),
                        codeArea.getText(0, codeArea.getCaretPosition()).split("\n").length),
                        codeArea.caretPositionProperty()));
        
        codeArea.replaceText(fileLoadedVM.getMarkDownText());
    }



    public void resetSelection(IndexRange selectedText, int difference) {
        codeArea.selectRange(selectedText.getStart() + difference, selectedText.getEnd() + difference);
    }

    public void resetCaretPosition(int caretPositon) {
        codeArea.displaceCaret(caretPositon);
    }

    public IndexRange getSelection() {
        return codeArea.getSelection();
    }

    public int getCurrentLine() {
        return codeArea.getText(0, codeArea.getCaretPosition()).split("\n").length;
    }

    public int getCaretPosition() {
        return codeArea.getCaretPosition();
    }

    private void setLocale(String lang) {
        Locale locale = new Locale(lang);
        ResourceBundle bundle = ResourceBundle.getBundle("config.lang", locale);
    }
}
