package view;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import launcher.Main;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

public class ImageLinkPicker {
    private Locale locale;
    private boolean mustBeImage;
    private boolean isAdded = false;

    @FXML
    private Label textLabel, errorTextLabel, linkLabel, errorLinkLabel;
    @FXML
    private TextField linkTxtField, textTxtField;

    @FXML
    private Button browseBtn, addBtn, cancelBtn;

    public String[] getValues() {
        if (!isAdded | linkTxtField.getText().trim().isEmpty() |
                (!mustBeImage && textTxtField.getText().trim().isEmpty())) {
            return null;
        }

        String[] values = new String[2];
        values[0] = textTxtField.getText();
        values[1] = linkTxtField.getText();
        return values;
    }

    public void initialize(String text, boolean mustBeImage) {
        this.mustBeImage = mustBeImage;
        textTxtField.setText(text);
        addBtn.setDisable(true);

        setLocale(Locale.getDefault().toString());

        if (mustBeImage) {
            textLabel.setVisible(false);
            textLabel.setManaged(false);
            textTxtField.setVisible(false);
            textTxtField.setManaged(false);
            errorTextLabel.setVisible(false);
            errorTextLabel.setManaged(false);
        } else {
            browseBtn.setVisible(false);
            browseBtn.setManaged(false);
            if (text.trim().isEmpty()) {
                errorTextLabel.setText(getStringValue("errorEmptyFieldLabel"));
            }
        }

        errorLinkLabel.setText(getStringValue("errorEmptyFieldLabel"));

        textTxtField.textProperty().addListener((__) -> {
            if (!mustBeImage && textTxtField.getText().trim().isEmpty()) {
                errorTextLabel.setText(getStringValue("errorEmptyFieldLabel"));
                addBtn.setDisable(true);
            } else {
                errorTextLabel.setText("");
                if (errorLinkLabel.getText().isEmpty()) {
                    addBtn.setDisable(false);
                }
            }
        });

        linkTxtField.textProperty().addListener((__) -> {
            if (linkTxtField.getText().trim().isEmpty()) {
                errorLinkLabel.setText(getStringValue("errorEmptyFieldLabel"));
                addBtn.setDisable(true);
            } else if (mustBeImage && !linkTxtField.getText().matches(".+[.](jpg|jpeg|png)$")) {
                errorLinkLabel.setText(getStringValue("errorImageFormatLabel"));
                addBtn.setDisable(true);
            } else {
                errorLinkLabel.setText("");
                if (errorTextLabel.getText().isEmpty()) {
                    addBtn.setDisable(false);
                }
            }
        });
    }

    @FXML
    private void close() {
        var stage = (Stage)cancelBtn.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void add() {
        isAdded = true;
        close();
    }

    @FXML
    private void browseImage() throws IOException {
        var link = Main.openImageFileDialog();
        if (!link.isEmpty()) {
            linkTxtField.setText("file:/" + link);
        }

    }

    private void setLocale(String lang) {
        locale = new Locale(lang);
        ResourceBundle bundle = ResourceBundle.getBundle("config.lang", locale);

        textLabel.setText(bundle.getString("textLabel"));
        linkLabel.setText(bundle.getString("linkLabel"));

        browseBtn.setText(bundle.getString("browseBtn"));
        addBtn.setText(bundle.getString("addBtn"));
        cancelBtn.setText(bundle.getString("cancelBtn"));
    }

    private String getStringValue(String name) {
        ResourceBundle bundle = ResourceBundle.getBundle("config.lang", locale);
        return bundle.getString(name);
    }
}
