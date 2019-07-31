package view;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.util.Locale;
import java.util.ResourceBundle;

public class Help {
    @FXML
    private Label helpLabel;

    @FXML
    private void initialize() {
        setLocale(Locale.getDefault().toString());
    }

    private void setLocale(String lang) {
        Locale locale = new Locale(lang);
        ResourceBundle bundle = ResourceBundle.getBundle("config.lang", locale);

        helpLabel.setText(bundle.getString("helpLabel"));
    }
}
