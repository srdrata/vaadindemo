package com.serdar.ata.vaadindemo;

import com.vaadin.server.FileDownloader;
import com.vaadin.server.StreamResource;
import com.vaadin.shared.ui.ValueChangeMode;
import com.vaadin.ui.*;

/**
 * @author Serdar.Ata
 * Created by Serdar.Ata  on 24/06/2019.
 */
public class DownloadOperationsWindow extends Window {

    private String filename;

    public DownloadOperationsWindow() {
        super("Download File");
        TextField fileNameText = new TextField("Write File Name To Download");
        Button saveButton = new Button("Save");
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


        //TODO bunu kullanıcıdan gelmesini sağlamalıyız
        FileStreamSource streamSource = new FileStreamSource("D:/aaaaaa.txt");

        fileNameText.addValueChangeListener(e->{
            if (fileNameText.getValue().isEmpty()) {
                Notification.show("File name is empty");
            } else {
                this.filename = fileNameText.getValue();
                // setFilename(fileNameText.getValue());

                StreamResource streamResource = new StreamResource(streamSource, filename + ".txt");
                FileDownloader fileDownloader = new FileDownloader(streamResource);
                fileDownloader.extend(saveButton);
            }
        });
        fileNameText.setValueChangeMode(ValueChangeMode.BLUR);

        cancelButton.addClickListener(clickEvent -> close());

    }
}



//    public void downloadFile(Button component) {
//        if (!StringUtils.isEmpty(getFilename())) {
//            File tmpFile = new File("D:/" + getFilename() + ".txt");
//            FileInputStream fileInputStream = null;
//
//            try {
//                tmpFile.createNewFile();
//            } catch (IOException ex) {
//                System.out.println(ex.getMessage());
//            }
//
//            try {
//                fileInputStream = new FileInputStream(tmpFile);
//            } catch (FileNotFoundException ex) {
//                System.out.println(ex.getMessage());
//            }
//
//            if (fileInputStream != null) {
//                try{
//                    StreamResource downloadResource = createFileResource(fileInputStream);
//                    downloadResource.setFilename(getFilename() + ".txt");
//                    FileDownloader fileDownloader = new FileDownloader(downloadResource);
//                    //fileDownloader.setFileDownloadResource(downloadResource);
//                    fileDownloader.extend(component);
//                    fileInputStream.close();
//                }catch(IOException ex){
//                    System.out.println(ex.getMessage());
//                }
//
//            }
//        }
//    }