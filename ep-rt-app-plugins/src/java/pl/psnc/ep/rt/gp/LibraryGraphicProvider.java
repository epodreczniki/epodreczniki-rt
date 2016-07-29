package pl.psnc.ep.rt.gp;

import java.util.Map;

import pl.psnc.dlibra.app.extension.graphicprovider.DefaultGraphicProvider;

public class LibraryGraphicProvider extends DefaultGraphicProvider {

    public LibraryGraphicProvider() {
        resourceDirectory = "/img/";
    }


    @Override
    protected Class<?> getClassForResourceSearch() {
        return this.getClass();
    }


    public void initialize(Map<String, Object> initPrefs) {
    }


    @Override
    public String toString() {
        return "dLibra standard graphics";
    }
}
