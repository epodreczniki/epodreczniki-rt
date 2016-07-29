package pl.psnc.ep.rt.gp;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import com.kitfox.svg.app.beans.SVGIcon;

import pl.psnc.dlibra.app.extension.miniatureprovider.MiniatureProvider;

public class SVGMiniatureProvider implements MiniatureProvider {

    private SVGIcon icon;


    @Override
    public void initialize(Map<String, Object> initPrefs) {
    }


    @Override
    public int countMiniatures() {
        return 1;
    }


    @Override
    public void dispose() {
    }


    @Override
    public Image getMiniature(int index)
            throws IOException {
        Dimension size = icon.getPreferredSize();
        BufferedImage image = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_ARGB);
        icon.setAntiAlias(true);
        icon.setScaleToFit(true);
        icon.paintIcon(null, image.getGraphics(), 0, 0);
        return image;
    }


    @Override
    public boolean setMainFile(File mainFile) {
        icon = new SVGIcon();
        try {
            icon.setSvgURI(mainFile.toURI());
        } catch (Exception e) {
            return false;
        }
        return icon.getIconWidth() != 0;
    }


    @Override
    public MiniatureProvider clone()
            throws CloneNotSupportedException {
        return (MiniatureProvider) super.clone();
    }
}
