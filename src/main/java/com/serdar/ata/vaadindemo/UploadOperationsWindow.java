package com.serdar.ata.vaadindemo;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.vaadin.annotations.StyleSheet;
import com.vaadin.event.dd.acceptcriteria.AcceptAll;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.Page;
import com.vaadin.server.StreamVariable;
import com.vaadin.shared.Registration;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.dnd.FileDropHandler;
import com.vaadin.ui.dnd.FileDropTarget;
import com.vaadin.ui.dnd.event.FileDropEvent;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.viritin.label.MLabel;
import org.vaadin.viritin.layouts.MVerticalLayout;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.Collator;
import java.util.*;
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

    private VerticalLayout fileArea;


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
        fileArea = new VerticalLayout();
        fileArea.setMargin(false);
        fileArea.setSpacing(true);

        mainLayout = new VerticalLayout();
        mainLayout.addComponent(fileArea);
        fileArea.setWidth(100, Unit.PERCENTAGE);
        buttonLayout = new HorizontalLayout();
        buttonLayout.addComponents(saveButton, cancelButton);

        buttonLayout.setComponentAlignment(saveButton, Alignment.MIDDLE_LEFT);
        buttonLayout.setComponentAlignment(cancelButton, Alignment.MIDDLE_RIGHT);

        setPosition(20, 150);
        setWidth("600px");
        //setHeight("400px");
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

    }

    protected  void initProgressPanel(){
        progressPanel = new Panel("deneme");
        progressPanel.setSizeFull();
        progressPanel.setStyleName(ValoTheme.PANEL_WELL);
    }

    @Override
    public void setContent(Component content) {
        super.setContent(content);
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


            event.getFiles().forEach(file -> {

                FileUploadProgress fileProgress = new FileUploadProgress(file.getFileName());
                //fileProgress.setWidth(100, Unit.PERCENTAGE);
                fileArea.addComponent(fileProgress);
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
                        float value = event.getBytesReceived() / (float)event.getContentLength();
                        fileProgress.setValue(value);
                    }

                    @Override
                    public void streamingStarted(StreamingStartEvent event) {

                    }

                    @Override
                    public void streamingFinished(StreamingEndEvent event) {

                    }

                    @Override
                    public void streamingFailed(StreamingErrorEvent event) {

                    }

                    @Override
                    public boolean isInterrupted() {
                        return false;
                    }
                });
            });
        });

//        mainLayout.addComponent(dropPanel);
    }

    protected  void initUpload(){
        upload = new Upload(null, receiver);
        upload.addFailedListener(this);
        upload.addSucceededListener(this);
        upload.addStartedListener(this);
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

        FileUploadProgress fileProgress = new FileUploadProgress(event.getFilename());
        event.getUpload().addProgressListener(new Upload.ProgressListener() {
            @Override
            public void updateProgress(long readBytes, long contentLength) {
                fileProgress.setValue( readBytes / (float)contentLength);
            }
        });

        fileArea.addComponent(fileProgress);
        //fileProgress.setWidth(100, Unit.PERCENTAGE);
        System.out.println("upload started");
    }

    @Override
    public void uploadSucceeded(Upload.SucceededEvent event) {
        System.out.println("upload succeed");
    }

    class FileUploadProgress extends HorizontalLayout
    {
        Label filenameLabel;
        ProgressBar progress;

        FileUploadProgress(String filename)
        {
            this.setMargin(false);
            this.setWidth("100%");

            this.addStyleName("red");

            filenameLabel = new Label(filename);
            filenameLabel.setWidth("100%");
            this.addComponent(filenameLabel);


            progress = new ProgressBar();
            HorizontalLayout progressLayout = new HorizontalLayout();
            progressLayout.addComponent(progress);
            progressLayout.setWidth(120, Unit.PIXELS);
            progress.setWidth(100, Unit.PERCENTAGE);

            this.addComponent(progressLayout);
            this.setExpandRatio(progressLayout, 0f);
            this.setExpandRatio(filenameLabel, 1f);


        }

        public void setValue(float value)
        {
            progress.setValue(value);
        }


    }

}
/*
class MyUploadPanel extends FileDropTarget implements FileDropHandler{


    public MyUploadPanel(Component root) {
        super(root);

    }
    @Override
    public void drop(FileDropEvent event) {
        Collection<Html5File> files = event.getFiles();

        if (files != null){
            Collection<Html5File> filesToUpload = files;
            for (Html5File file: filesToUpload){
                file.setStreamVariable(new StreamVariable() {
                    @Override
                    public OutputStream getOutputStream() {
                        return null;
                    }

                    @Override
                    public boolean listenProgress() {
                        return false;
                    }

                    @Override
                    public void onProgress(StreamingProgressEvent event) {

                    }

                    @Override
                    public void streamingStarted(StreamingStartEvent event) {

                    }

                    @Override
                    public void streamingFinished(StreamingEndEvent event) {

                    }

                    @Override
                    public void streamingFailed(StreamingErrorEvent event) {

                    }

                    @Override
                    public boolean isInterrupted() {
                        return false;
                    }
                });
            }
        }
    }


    public AcceptCriterion getAcceptCriterion(){
        return AcceptAll.get();
    }


}
*/


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
