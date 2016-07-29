package pl.psnc.ep.rt.customize;

import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import pl.psnc.dlibra.app.extension.language.InterfaceLanguage;

public class PolishLanguage implements InterfaceLanguage {

    public ResourceBundle getResourceBundle()
            throws MissingResourceException {
        return ResourceBundle.getBundle("dlibra-GUI", new Locale("pl"));
    }


    public void initialize(Map<String, Object> initPrefs) {
        new GUICustomizer().init(ResourceBundle.getBundle("GUI", Locale.getDefault()));
    }


    @Override
    public String toString() {
        return "Polish intefrace language";
    }
}
