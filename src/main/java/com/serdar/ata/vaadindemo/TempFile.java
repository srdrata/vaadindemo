package com.serdar.ata.vaadindemo;

/**
 * @author Serdar.Ata
 * Created by Serdar.Ata  on 26/06/2019.
 */
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.UUID;

public class TempFile {
    private static final Logger logger = LoggerFactory.getLogger(TempFile.class);

    private File file;
    private boolean available;
    private String name;

    public TempFile(String name) {
        this(name, ".tmp");
    }

    public TempFile(String name, String extension) {
        this.name = name;
        try {
            file = File.createTempFile(String.format("aynatemp-%s", UUID.randomUUID().toString()), extension);
            available = true;
        } catch (IOException e) {
            logger.warn(e.getMessage(), e);
            //			name = String.format("aynatemp-%s%s", UUID.randomUUID().toString(), extension);
            //			file = new File(getTempDir(), name);
            available = false;
        }
    }

    public static File getTempDir() {
        return new File(System.getProperty("java.io.tmpdir"));
    }

    public File getFile() {
        if (available) {
            return file;
        } else {
            return null;
        }
    }

    public FileInputStream getFis() {
        if (available) {
            try {
                return new FileInputStream(file);
            } catch (FileNotFoundException e) {
                return null;
            }
        }
        return null;
    }

    public FileOutputStream getFos() {
        if (available) {
            try {
                return new FileOutputStream(file);
            } catch (FileNotFoundException e) {
                logger.error(e.getMessage(), e);
                return null;
            }
        }
        return null;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return file.getPath();
    }

    public void delete() {
        file.delete();
        available = false;
    }

    @Override
    protected void finalize() throws Throwable {
        delete();
        super.finalize();
    }

}

