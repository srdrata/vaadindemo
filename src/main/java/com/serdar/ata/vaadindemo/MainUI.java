package com.serdar.ata.vaadindemo;

import com.vaadin.annotations.Push;
import com.vaadin.annotations.Widgetset;
import com.vaadin.server.StreamResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.ui.ui.Transport;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.UI;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * @author Serdar.Ata
 * Created by Serdar.Ata  on 21/06/2019.
 */


@Widgetset("com.serdar.ata.vaadindemo.AppWidgetSet")
@SpringUI(path = "")
@Push(transport = Transport.WEBSOCKET)
public class MainUI extends UI {


    @Override
    protected void init(VaadinRequest vaadinRequest) {
        HorizontalLayout mainLayout = new HorizontalLayout();
        mainLayout.setSpacing(true);
        mainLayout.setMargin(true);

        Button openDownloadDialog = new Button("Open Download Dialog");
        Button openUploadDialog = new Button("Open Upload Dialog");
        Button uploadProgressDeneme = new Button("Upload progress ");
        Button multiFileUploadDeneme = new Button("MultiFile Upload");

        mainLayout.addComponents(openDownloadDialog, openUploadDialog, uploadProgressDeneme, multiFileUploadDeneme);

        setContent(mainLayout);

        openDownloadDialog.addClickListener(clickEvent -> {

            FileExporter modalWindow = new FileExporter("filename.txt", new Consumer<OutputStream>() {
                @Override
                public void accept(OutputStream outputStream) {

                    try {

                        System.out.println("Writer to outstream");
                        outputStream.write(("Random generated UUID " + UUID.randomUUID().toString()) .getBytes());



                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

            UI.getCurrent().addWindow(modalWindow);
        });

        openUploadDialog.addClickListener(clickEvent -> {
            UploadOperationsWindow uploadOperationsWindow = new UploadOperationsWindow();
            UI.getCurrent().addWindow(uploadOperationsWindow);
        });

        uploadProgressDeneme.addClickListener(clickEvent -> {
            UploadWithProgressDenemeWindow uploadWithProgressDenemeWindow = new UploadWithProgressDenemeWindow();
            UI.getCurrent().addWindow(uploadWithProgressDenemeWindow);
        });

//        multiFileUploadDeneme.addClickListener(clickEvent -> {
//            UploadMultiFileUploadDeneme uploadMultiFileUploadDeneme = new UploadMultiFileUploadDeneme();
//            UI.getCurrent().addWindow(uploadMultiFileUploadDeneme);
//        });

    }

    private StreamResource createFileResource(FileInputStream fileInputStream) {
        StreamResource sr = new StreamResource(new StreamResource.StreamSource() {
            @Override
            public InputStream getStream() {
                return fileInputStream;
            }
        }, "");

        sr.setCacheTime(0);
        return sr;
    }

}