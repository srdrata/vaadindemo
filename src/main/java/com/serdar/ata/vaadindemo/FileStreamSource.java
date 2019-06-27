package com.serdar.ata.vaadindemo;

import com.vaadin.server.StreamResource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Serdar.Ata
 * Created by Serdar.Ata  on 21/06/2019.
 */
public class FileStreamSource implements StreamResource.StreamSource
{
    private static final long serialVersionUID = 1L;
    String name;
    FileStreamSource(String name)
    {
        this.name = name;
    }

    @Override
    public InputStream getStream()
    {
        File tempFile = new File(name);
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(tempFile);
            return fileInputStream;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}