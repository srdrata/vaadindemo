package com.serdar.ata.vaadindemo;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.StreamVariable;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.dnd.FileDropTarget;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.viritin.label.MLabel;
import org.vaadin.viritin.layouts.MVerticalLayout;

import java.io.OutputStream;


public class UploadOperationsWindow extends Window  implements Upload.FinishedListener, Upload.StartedListener, Upload.FailedListener, Upload.SucceededListener {

//    private Button saveButton ;
//    private Button cancelButton ;
//    private HorizontalLayout buttonLayout ;

    private Upload upload;
    private Upload.Receiver receiver;
    private VerticalLayout dropLayout;
    private Panel dropPanel;
    private TempFile tempFile;
    private VerticalLayout fileArea;

    private boolean dropTargetInterrupted;

    public UploadOperationsWindow(){
        super("Upload File");

        this.receiver = (filename, mimeType) -> {
            tempFile = new TempFile(filename);
            return tempFile.getFos();
        };

        init();

//        saveButton.addClickListener(clickEvent -> {
//            //TODO DocumentService->DocumentUploadComponent->init() ve initDropLabel
//            upload.submitUpload();
//            System.out.println( "read bytes" + String.valueOf(upload.getBytesRead()) + "get upload size: " + String.valueOf(upload.getUploadSize()));
//        });
//
//        cancelButton.addClickListener(clickEvent ->{
//            close();
//        } );
    }

    private void init(){

        fileArea = new VerticalLayout();
        fileArea.setMargin(false);
        fileArea.setSpacing(true);

        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.addComponent(fileArea);
        fileArea.setWidth(100, Unit.PERCENTAGE);

//        saveButton = new Button("Save");
//        cancelButton = new Button("Cancel");
//        buttonLayout = new HorizontalLayout();
//        buttonLayout.addComponents(saveButton, cancelButton);
//        buttonLayout.setComponentAlignment(saveButton, Alignment.MIDDLE_LEFT);
//        buttonLayout.setComponentAlignment(cancelButton, Alignment.MIDDLE_RIGHT);

        setPosition(25, 150);
        setWidth("600px");
        setModal(true);
        setContent(mainLayout);


        initDropPanel();
        initUpload();

        mainLayout.addComponents(dropPanel);

        MVerticalLayout components = new MVerticalLayout().withUndefinedSize().withDefaultComponentAlignment(Alignment.MIDDLE_CENTER).withSpacing(true).withFullWidth().with(
                new MLabel(VaadinIcons.UPLOAD.getHtml()).withContentMode(ContentMode.HTML).withStyleName(ValoTheme.LABEL_COLORED, "upload-icon"),
                new MLabel("Dosyayı sürükle-bırak").withStyleName(ValoTheme.LABEL_LARGE),
                new MLabel("Ya da").withStyleName(ValoTheme.LABEL_LARGE),
                upload
        );

        dropLayout.addComponent(components);
        dropLayout.setComponentAlignment(components, Alignment.MIDDLE_CENTER);

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

        new FileDropTarget<>(dropLayout, event -> {

            dropTargetInterrupted = false;

            event.getFiles().forEach(file -> {

                FileUploadProgress fileProgress = new FileUploadProgress(file.getFileName());
                Button deleteButton = (Button) fileProgress.getComponent(2);
                deleteButton.addClickListener(e -> {
                    fileProgress.setVisible(false);
                    dropTargetInterrupted = true;
                    this.tempFile.delete();
                });
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
                        return dropTargetInterrupted;
                    }
                });
            });
        });
    }

    protected  void initUpload(){

        upload = new Upload(null, receiver);
        upload.addFailedListener(this);
        upload.addSucceededListener(this);
        upload.addStartedListener(this);
        upload.setImmediateMode(true);
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

        Button deleteButton = (Button) fileProgress.getComponent(2);
        deleteButton.addClickListener(e -> {
            upload.interruptUpload();
            fileProgress.setVisible(false);
            this.tempFile.delete();
        });

        event.getUpload().addProgressListener(new Upload.ProgressListener() {
            @Override
            public void updateProgress(long readBytes, long contentLength) {
                fileProgress.setValue( readBytes / (float)contentLength);
            }
        });

        fileArea.addComponent(fileProgress);
        System.out.println("upload started");
    }

    @Override
    public void uploadSucceeded(Upload.SucceededEvent event) {

        System.out.println("upload succeed");
    }

    //------------------------------------------------

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


            Button deleteButton = new Button("delete");
            deleteButton.setId("uploadDeleteButton");
            deleteButton.addStyleName(ValoTheme.BUTTON_DANGER);
            deleteButton.addStyleName(ValoTheme.BUTTON_BORDERLESS);
            deleteButton.addStyleName(ValoTheme.BUTTON_ICON_ONLY);
            deleteButton.setIcon(VaadinIcons.CLOSE_CIRCLE);

            this.addComponent(progressLayout);
            this.addComponent(deleteButton);
            this.setExpandRatio(progressLayout, 0f);
            this.setExpandRatio(filenameLabel, 10f);
            this.setExpandRatio(deleteButton, 1f);


            this.setComponentAlignment(filenameLabel, Alignment.TOP_CENTER);
            this.setComponentAlignment(progressLayout, Alignment.BOTTOM_LEFT);
            this.setComponentAlignment(deleteButton, Alignment.TOP_CENTER);
        }

        public void setValue(float value)
        {
            progress.setValue(value);
        }

    }

}