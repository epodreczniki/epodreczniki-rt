package pl.psnc.ep.rt.web.womi;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;

public class JsonExtractor {

    private static final String TEMPLATE_FILE = "swiffy-template.html";

    private static final Pattern SCRIPT_PATTERN = Pattern.compile("swiffyobject[^{]+(\\{.*\\})");

    private static final Pattern STYLE_PATTERN = Pattern.compile("<div id=\"swiffycontainer\" style=\"([^\"]+)\"");

    private static final Pattern ENGINE_VERSION_PATTERN = Pattern
            .compile("<script.+src=\"https://www.gstatic.com/swiffy/v([^/]+)/");

    private static final String DEFAULT_ENGINE_VERSION = "5.2";

    private final String content;


    public JsonExtractor(String content) {
        this.content = content;
    }


    public String buildSwiffyFromTemplate() {
        String json = getJson();
        String style = getStyle();
        String version = getEngineVersion();
        try {
            String template = getTemplate();
            template = replaceInContent(template, json, style, version);
            return template;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    String getJson() {
        Matcher m = SCRIPT_PATTERN.matcher(content);
        m.find();
        return m.group(1);
    }


    String getStyle() {
        Matcher m2 = STYLE_PATTERN.matcher(content);
        m2.find();
        return m2.group(1);
    }


    String getEngineVersion() {
        Matcher m3 = ENGINE_VERSION_PATTERN.matcher(content);
        if (m3.find()) {
            return m3.group(1);
        } else {
            return DEFAULT_ENGINE_VERSION;
        }
    }


    private String getTemplate()
            throws IOException {
        InputStream is = this.getClass().getResourceAsStream(TEMPLATE_FILE);
        StringWriter stringWriter = new StringWriter();
        IOUtils.copy(is, stringWriter);
        return stringWriter.toString();
    }


    private String replaceInContent(final String template, final String json, final String style,
            final String engineVersion) {
        String temp = template.replace("${{{SwiffyObject}}}", json);
        temp = temp.replace("${{{STYLE}}}", style);
        temp = temp.replace("${{{ENGINE_VERSION}}}", engineVersion);
        return temp;
    }

}
