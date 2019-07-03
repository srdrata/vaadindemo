package com.serdar.ata.vaadindemo;

import com.vaadin.annotations.Push;
import com.vaadin.annotations.Widgetset;
import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.ui.ui.Transport;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.UI;

import java.io.OutputStream;
import java.util.Arrays;
import java.util.UUID;

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

//        mainLayout.addComponents(openDownloadDialog, openUploadDialog, uploadProgressDeneme, multiFileUploadDeneme);
        mainLayout.addComponents(openDownloadDialog, openUploadDialog);

        setContent(mainLayout);

        openDownloadDialog.addClickListener(clickEvent -> {

            String export =  "Random generated UUID " + Arrays.toString(UUID.randomUUID().toString().getBytes());

            FileExporter modalWindow = new FileExporter("filename.txt");
            OutputStream os = modalWindow.getOutputStream();

            try {
                System.out.println("Writer to outstream");
                os.write(("Random generated UUID " + UUID.randomUUID().toString()).getBytes());
                os.close();
            }catch(Exception e){}
        });

        openUploadDialog.addClickListener(clickEvent -> {
            UploadOperationsWindow uploadOperationsWindow = new UploadOperationsWindow();
            UI.getCurrent().addWindow(uploadOperationsWindow);
        });

//        uploadProgressDeneme.addClickListener(clickEvent -> {
//            UploadWithProgressDenemeWindow uploadWithProgressDenemeWindow = new UploadWithProgressDenemeWindow();
//            UI.getCurrent().addWindow(uploadWithProgressDenemeWindow);
//        });

//        multiFileUploadDeneme.addClickListener(clickEvent -> {
//            UploadMultiFileUploadDeneme uploadMultiFileUploadDeneme = new UploadMultiFileUploadDeneme();
//            UI.getCurrent().addWindow(uploadMultiFileUploadDeneme);
//        });

    }

}