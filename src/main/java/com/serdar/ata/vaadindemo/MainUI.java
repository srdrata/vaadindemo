package com.serdar.ata.vaadindemo;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Widgetset;
import com.vaadin.server.StreamResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.*;

import java.io.*;

/**
 * @author Serdar.Ata
 * Created by Serdar.Ata  on 21/06/2019.
 */


@Widgetset("com.serdar.ata.vaadindemo.AppWidgetSet")
@SpringUI(path = "")
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

            DownloadOperationsWindow modalWindow = new DownloadOperationsWindow();
            UI.getCurrent().addWindow(modalWindow);

//            if (modalWindow != null) {
//               modalWindow.addCloseListener(closeEvent -> {
//                   if (! StringUtils.isEmpty(modalWindow.getFilename())) {
//                       File tmpFile = new File("D:/" + modalWindow.getFilename() + ".txt");
//                       FileInputStream fileInputStream = null;
//
//                       try {
//                           tmpFile.createNewFile();
//                       } catch (IOException ex) {
//                           System.out.println(ex.getMessage());
//                       }
//
//                       try {
//                           fileInputStream = new FileInputStream(tmpFile);
//                       } catch (FileNotFoundException ex) {
//                           System.out.println(ex.getMessage());
//                       }
//
//                       if (fileInputStream != null) {
//                           StreamResource downloadResource = createFileResource(fileInputStream);
//                           downloadResource.setFilename(modalWindow.getFilename() + ".txt");
//                           FileDownloader fileDownloader = new FileDownloader(downloadResource);
//                           fileDownloader.setFileDownloadResource(downloadResource);
//                           fileDownloader.extend(clickEvent.getButton());
//                       }
//                   }
//               });
//            }

        });

        openUploadDialog.addClickListener(clickEvent -> {
            UploadOperationsWindow uploadOperationsWindow = new UploadOperationsWindow();
            UI.getCurrent().addWindow(uploadOperationsWindow);
        });

        uploadProgressDeneme.addClickListener(clickEvent -> {
            UploadWithProgressDenemeWindow uploadWithProgressDenemeWindow = new UploadWithProgressDenemeWindow();
            UI.getCurrent().addWindow(uploadWithProgressDenemeWindow);
        });

        multiFileUploadDeneme.addClickListener(clickEvent -> {
            UploadMultiFileUploadDeneme uploadMultiFileUploadDeneme = new UploadMultiFileUploadDeneme();
            UI.getCurrent().addWindow(uploadMultiFileUploadDeneme);
        });



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