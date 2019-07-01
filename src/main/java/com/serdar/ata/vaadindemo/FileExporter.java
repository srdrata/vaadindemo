package com.serdar.ata.vaadindemo;

import com.vaadin.server.AbstractClientConnector;
import com.vaadin.server.EventTrigger;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.*;

import java.io.*;
import java.util.function.Consumer;

/**
 * @author Serdar.Ata
 * Created by Serdar.Ata  on 24/06/2019.
 */
public class FileExporter extends Window {

    private String filename;
    Consumer writer;
    FileDownloader fileDownloader;

    public FileExporter(String filename, Consumer <OutputStream> writer) {
        super("Save As");

        this.writer = writer;
        TextField fileNameText = new TextField();
        fileNameText.setValue(filename);

        Button saveButton = new Button("OK");
        Button cancelButton = new Button("Cancel");

        VerticalLayout mainLayout = new VerticalLayout();
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.addComponents(saveButton, cancelButton);
        mainLayout.addComponents(fileNameText, buttonLayout);
        mainLayout.setMargin(true);
        setPosition(20, 150);
        setWidth("210px");
        setHeight("210px");
        setModal(true);
        setContent(mainLayout);

        fileNameText.selectAll();
        fileNameText.focus();

        StreamResource resource = new StreamResource(new StreamResource.StreamSource() {
            @Override
            public InputStream getStream() {

                System.out.println("get stream ");

                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                writer.accept(bos);
                byte[] data = bos.toByteArray();
                return new ByteArrayInputStream(data);
            }
        }, fileNameText.getValue());

        saveButton.addClickListener(new Button.ClickListener() {
                                        @Override
                                        public void buttonClick(Button.ClickEvent event) {

                                            System.out.println("btn click:" + fileNameText.getValue());

                                            StreamResource resource1 = new StreamResource(new StreamResource.StreamSource() {
                                                @Override
                                                public InputStream getStream() {

                                                    System.out.println("get stream ");

                                                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                                                    writer.accept(bos);
                                                    byte[] data = bos.toByteArray();
                                                    return new ByteArrayInputStream(data);
                                                }
                                            }, fileNameText.getValue());
                                            resource1.setCacheTime(0);


                                            fileDownloader.setFileDownloadResource(resource1);
                                        }
                                    });

        fileDownloader = new FileDownloader(resource);
        fileDownloader.extend(saveButton);
        cancelButton.addClickListener(clickEvent -> close());

    }


}

