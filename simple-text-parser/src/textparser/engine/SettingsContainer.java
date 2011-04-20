package textparser.engine;
import java.awt.Dimension;
import java.awt.Point;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import javax.swing.JFrame;

public class SettingsContainer implements SettingsContainerConstants {
    private Properties properties = new Properties();
    private String settingsFilePath;
    public static final String ITERABLE_SEPARATOR = "|";

    public SettingsContainer(String pathToSettingsFile) {
        this.settingsFilePath = pathToSettingsFile;
        load();
    }

    public void put(JFrame frame) {
        if (frame != null) {
            put(frame.getTitle(), frame);
        }
    }

    public void put(String title, JFrame frame) {
        if (frame != null) {
            properties.put(title + _WIDTH, Integer.toString(frame.getWidth()));
            properties.put(title + _HEIGHT, Integer.toString(frame.getHeight()));
            Point p = frame.getLocationOnScreen();
            properties.put(title + _X, Integer.toString(p.x));
            properties.put(title + _Y, Integer.toString(p.y));
        }
    }

    public void get(JFrame frame) {
        if (frame != null) {
            get(frame.getTitle(), frame);
        }
    }

    public void get(String title, JFrame frame) {
        if (frame != null) {
            String width = properties.getProperty(title + _WIDTH, DEFAULT_WIDTH);
            String height = properties.getProperty(title + _HEIGHT, DEFAULT_HEIGTH);
            String x = properties.getProperty(title + _X, "0");
            String y = properties.getProperty(title + _Y, "0");

            frame.setBounds(new Integer(x), new Integer(y), new Integer(width), new Integer(height));
        }
    }

    public void put(String title, Dimension dim) {
        if (dim != null) {
            properties.put(title + _WIDTH, Integer.toString(dim.width));
            properties.put(title + _HEIGHT, Integer.toString(dim.height));
        }
    }

    public Dimension getDimension(String title) {
        String width = properties.getProperty(title + _WIDTH, DEFAULT_ZERO);
        String height = properties.getProperty(title + _HEIGHT, DEFAULT_ZERO);
        return new Dimension(Integer.parseInt(width), Integer.parseInt(height));
    }

    public void put(String key, Iterable<?> iterable) {//TODO save values in single line: name 1^2^3^4
        if (iterable != null) {
            StringBuilder sb = new StringBuilder();
            int index = 0;
            for (Object object : iterable) {
                sb.append(object);
                sb.append(ITERABLE_SEPARATOR);
                //properties.put(key + index++, object);
            }
            properties.put(ITERABLE_ + key, sb.toString());
        }
    }

    public void get(String key, Iterable<?> iterable) {
        int size = Integer.parseInt(properties.get(ITERABLE_SIZE_ + key).toString());
        if (size > 0 && iterable != null) {
//            for (Object object : iterable) {//walk thought iterable
//            }
        }
    }

    public void put(String key, String value) {
        properties.put(key, value);
    }

    public void put(String key, Integer value) {
        properties.put(key, Integer.toString(value));
    }

    public String get(String key) {
        return properties.get(key).toString();
    }

    public Integer getInt(String key) {
        return Integer.parseInt(properties.get(key).toString());
    }

    public Long getLong(String key) {
        return Long.parseLong(properties.get(key).toString());
    }

    public void save() {
        try {
            FileOutputStream out = new FileOutputStream(settingsFilePath);
            properties.store(out, ".cfg");
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void load() {
        try {
            FileInputStream in = new FileInputStream(settingsFilePath);
            properties.load(in);
            in.close();
        } catch (IOException e) {
            //settings file not found - using defaults
        }
    }
}
