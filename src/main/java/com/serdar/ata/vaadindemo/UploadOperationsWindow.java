package com.serdar.ata.vaadindemo;

import com.vaadin.annotations.StyleSheet;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.StreamVariable;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.dnd.FileDropTarget;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.viritin.label.MLabel;
import org.vaadin.viritin.layouts.MVerticalLayout;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.Collator;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class UploadOperationsWindow extends Window  implements Upload.FinishedListener, Upload.StartedListener, Upload.FailedListener, Upload.SucceededListener {

    private File file;
    private Button saveButton ;
    private Button cancelButton ;

    private Upload upload;
    private Upload.Receiver receiver;

    private VerticalLayout mainLayout ;
    private HorizontalLayout buttonLayout ;

    private VerticalLayout dropLayout;
    private Panel dropPanel;

    private Panel progressPanel;

    private TempFile tempFile;


    public UploadOperationsWindow(){
        super("Upload File");

        this.receiver = (filename, mimeType) -> {
            tempFile = new TempFile(filename);
            return tempFile.getFos();
        };

        init();

        saveButton.addClickListener(clickEvent -> {
            //TODO DocumentService->DocumentUploadComponent->init() ve initDropLabel
            upload.submitUpload();
            System.out.println( "read bytes" + String.valueOf(upload.getBytesRead()) + "get upload size: " + String.valueOf(upload.getUploadSize()));
        });

        cancelButton.addClickListener(clickEvent ->{
            close();
        } );
    }

    private void init(){
        saveButton = new Button("Save");
        cancelButton = new Button("Cancel");

        mainLayout = new VerticalLayout();
        buttonLayout = new HorizontalLayout();
        buttonLayout.addComponents(saveButton, cancelButton);

        buttonLayout.setComponentAlignment(saveButton, Alignment.MIDDLE_LEFT);
        buttonLayout.setComponentAlignment(cancelButton, Alignment.MIDDLE_RIGHT);

        setPosition(20, 150);
        setWidth("400px");
        setHeight("400px");
        setModal(true);
        setContent(mainLayout);


        initDropPanel();
        initUpload();

        mainLayout.addComponents(dropPanel, buttonLayout);

        MVerticalLayout components = new MVerticalLayout().withUndefinedSize().withDefaultComponentAlignment(Alignment.MIDDLE_CENTER).withSpacing(true).withFullWidth().with(
                new MLabel(VaadinIcons.UPLOAD.getHtml()).withContentMode(ContentMode.HTML).withStyleName(ValoTheme.LABEL_COLORED, "upload-icon"),
                new MLabel("Dosyayı sürükle-bırak").withStyleName(ValoTheme.LABEL_LARGE),
                new MLabel("Ya da").withStyleName(ValoTheme.LABEL_LARGE),
                upload
        );

        dropLayout.addComponent(components);
        dropLayout.setComponentAlignment(components, Alignment.MIDDLE_CENTER);


        initProgressPanel();

        mainLayout.addComponent(progressPanel);
    }

    protected  void initProgressPanel(){
        progressPanel = new Panel("deneme");
        progressPanel.setSizeFull();
        progressPanel.setStyleName(ValoTheme.PANEL_WELL);
    }

    protected void initDropPanel(){
        dropLayout = new VerticalLayout();
        dropLayout.setSizeFull();

        dropPanel = new Panel(dropLayout);
        dropPanel.setSizeFull();
        dropPanel.setStyleName(ValoTheme.PANEL_WELL);
        //dropPanel.setStyleName(ValoTheme.PANEL_BORDERLESS);

        new FileDropTarget<>(dropLayout, event -> {
            Collection<Html5File> files = event.getFiles().stream().sorted((o1, o2) -> {
                //TODO control locale settings
                Locale locale = Locale.getDefault();
                Collator collator = Collator.getInstance(locale); // The Collator class performs locale-sensitive String comparison
                return collator.compare(o1.getFileName(), o2.getFileName()); //compare file names
            }).collect(Collectors.toList());

            if(!files.isEmpty()){
                List<UploadProgressPopup.FileInfo> fileInfoList = files.stream().map(html5File -> new UploadProgressPopup.FileInfo(html5File.getFileName(), html5File.getFileSize())).collect(Collectors.toList());
                UploadProgressPopup uploadProgressPopup = new UploadProgressPopup(fileInfoList);
                uploadProgressPopup.show();
                Boolean errorOccured = new Boolean(false);
                Integer numFiles = new Integer(files.size());
                UI.getCurrent().setPollInterval(1000); //

                for(Html5File file: files){
                    if(errorOccured){
                        break;
                    }

                    long fileSize = file.getFileSize();
                    file.setStreamVariable(new StreamVariable() {
                        @Override
                        public OutputStream getOutputStream() {
                            tempFile = new TempFile(file.getFileName());
                            return tempFile.getFos();
                        }

                        @Override
                        public boolean listenProgress() {
                            return true;
                        }

                        @Override
                        public void onProgress(StreamingProgressEvent event) {
                            String fileName = event.getFileName();
                            long bytesReceived = event.getBytesReceived();

                            double value = 0;

                            if (fileSize > 0) {
                                value = bytesReceived / fileSize;
                            }

                            if (value < 0) {
                                value = 0;
                            }

                            uploadProgressPopup.update(fileName, value);
                        }

                        @Override
                        public void streamingStarted(StreamingStartEvent event) {

                        }

                        @Override
                        public void streamingFinished(StreamingEndEvent event) {
                            System.out.println("Streaming finished " + event.toString());

                        }

                        @Override
                        public void streamingFailed(StreamingErrorEvent event) {
                            Notification.show("Upload failed " +  event.toString());
                            if (tempFile != null) {
                                tempFile.delete();
                                tempFile = null;
                            }
                            uploadProgressPopup.close();
                        }

                        @Override
                        public boolean isInterrupted() {
                            return false;
                        }
                    });
                }
            }



        });

//        mainLayout.addComponent(dropPanel);
    }

    protected  void initUpload(){
        upload = new Upload(null, receiver);
        upload.addFailedListener(this);
        upload.addSucceededListener(this);
        upload.setImmediateMode(true); //TODO setImmediateMode = false yapılıp ekrandan upload componenti gizlenip save butona basınca upload işleminin başlamasının sağlanması
        upload.setButtonStyleName(ValoTheme.BUTTON_PRIMARY);
        upload.setButtonCaption("Browse File");
        upload.setId("VLbsUpload");
    }

    @Override
    public void uploadFailed(Upload.FailedEvent event) {
        Notification.show("Upload Failed" + " " + event.getReason());
        UI.getCurrent().setPollInterval(-1);
    }

    @Override
    public void uploadFinished(Upload.FinishedEvent event) {

        System.out.println("upload finished");
    }

    @Override
    public void uploadStarted(Upload.StartedEvent event) {
        System.out.println("upload started");
    }

    @Override
    public void uploadSucceeded(Upload.SucceededEvent event) {
        System.out.println("upload succeed");
    }
}



//class FileUploader implements Upload.Receiver, Upload.SucceededListener{
//
//    @Override
//    public OutputStream receiveUpload(String filename, String mimeType) {
//        return null;
//    }
//
//    @Override
//    public void uploadSucceeded(Upload.SucceededEvent event) {
//
//    }
//}
