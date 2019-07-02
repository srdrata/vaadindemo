package com.serdar.ata.vaadindemo;

import com.vaadin.server.*;
import com.vaadin.ui.*;
import java.io.*;

public class FileExporter {

    private StreamResource resource;
    private Window window;

    public FileExporter(String filename) {

        TextField fileNameText = new TextField();
        fileNameText.setValue(filename);

        Button okButton = new Button("OK");
        Button cancelButton = new Button("Cancel");

        VerticalLayout mainLayout = new VerticalLayout();
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.addComponents(okButton, cancelButton);
        mainLayout.addComponents(fileNameText, buttonLayout);
        mainLayout.setMargin(true);

        window = new Window();
        window.setCaption("Save as");
        window.setPosition(20, 150);
        window.setWidth("210px");
        window.setHeight("210px");
        window.setModal(true);
        window.setContent(mainLayout);

        fileNameText.selectAll();
        fileNameText.focus();

        resource = new StreamResource(null, fileNameText.getValue());
        resource.setCacheTime(0);

        okButton.addClickListener(event -> {

            System.out.println("btn click");

            resource.setStreamSource(new StreamResource.StreamSource() {
                @Override
                public InputStream getStream() {
                    System.out.println("getStream");
                    return new ByteArrayInputStream(os.toByteArray());
                }
            });

            resource.setFilename(fileNameText.getValue());
        });

        FileDownloader fileDownloader = new CustomFileDownloader(resource);
        fileDownloader.extend(okButton);

        cancelButton.addClickListener(clickEvent -> window.close());
        UI.getCurrent().addWindow(window);

    }

    ByteArrayOutputStream os ;

    OutputStream getOutputStream()
    {
        if(os == null)
            os = new FileExportOutputStream();

        return os;
    }

    class FileExportOutputStream extends ByteArrayOutputStream
    {
        @Override
        public void close() throws IOException {
            super.close();

        }
    }

    class CustomFileDownloader extends FileDownloader{

        public CustomFileDownloader(Resource resource) {
            super(resource);
        }

        @Override
        public boolean handleConnectorRequest(VaadinRequest request, VaadinResponse response, String path) throws IOException {
            try {
                boolean b = super.handleConnectorRequest(request, response, path);
                return b;
            }catch(Exception e){
                e.printStackTrace();
                Notification.show("Error occured during download"). setDelayMsec(-1);
                return false;
            }finally {
                window.close();
            }
        }
    }

}

