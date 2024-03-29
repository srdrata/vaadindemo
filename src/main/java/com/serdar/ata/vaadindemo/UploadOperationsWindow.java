package com.serdar.ata.vaadindemo;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.Page;
import com.vaadin.server.StreamVariable;
import com.vaadin.shared.Registration;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.dnd.FileDropTarget;
import com.vaadin.ui.themes.ValoTheme;
import org.springframework.util.StringUtils;
import org.vaadin.viritin.label.MLabel;
import org.vaadin.viritin.layouts.MVerticalLayout;

import java.io.OutputStream;


public class UploadOperationsWindow extends Window  implements Upload.FinishedListener, Upload.StartedListener, Upload.FailedListener, Upload.SucceededListener {

    private Button saveButton ;
    private Button cancelButton ;
    private HorizontalLayout buttonLayout ;

    private Upload upload;
    private Registration progressListener;
    private Upload.Receiver receiver;
    private VerticalLayout dropLayout;
    private Panel dropPanel;
    private TempFile tempFile;
    private VerticalLayout fileArea;


    private boolean dropTargetInterrupted;

    public UploadOperationsWindow(){
        super("Upload File");
        Page.getCurrent().getStyles().add(".abc{ color:red !important;}");
        //Page.getCurrent().getStyles().add(".import-progress-done{color:green !important;}");
        Page.getCurrent().getStyles().add(".import-progress-done > .v-progressbar-wrapper > .v-progressbar-indicator{ border: 1px solid #189100; background-image: linear-gradient(to bottom,#20c200 2%, #189100 98%);}");

        Page.getCurrent().getStyles().add(".color-red {color: red !important}");
        Page.getCurrent().getStyles().add(".color-green {color: #189100 !important}");
        Page.getCurrent().getStyles().add(".custom-upload-button-style{display: none !important;}");
        //Page.getCurrent().getStyles().add(".custom-upload-style > div > .gwt-FileUpload{background-color: red !important;}");

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

        fileArea = new VerticalLayout();
        fileArea.setMargin(false);
        fileArea.setSpacing(true);

        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.addComponent(fileArea);
        fileArea.setWidth(100, Unit.PERCENTAGE);

        saveButton = new Button("Save");
        cancelButton = new Button("Cancel");
        buttonLayout = new HorizontalLayout();
        buttonLayout.addComponents(saveButton, cancelButton);
        buttonLayout.setComponentAlignment(saveButton, Alignment.MIDDLE_LEFT);
        buttonLayout.setComponentAlignment(cancelButton, Alignment.MIDDLE_RIGHT);

        setPosition(15, 150);
        setWidth("600px");
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
                HorizontalLayout iconsLayout = (HorizontalLayout) fileProgress.getComponent(2);
                Button deleteButton = (Button) iconsLayout.getComponent(0);
                Button fileUploadedButton = (Button) iconsLayout.getComponent(1);

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
                        if(fileUploadedButton != null){
                            fileUploadedButton.setVisible(false);
                        }
                    }

                    @Override
                    public void streamingFinished(StreamingEndEvent event) {
                        if(fileUploadedButton != null){
                            fileUploadedButton.setVisible(true);
                        }
                        HorizontalLayout progressLayout = (HorizontalLayout) fileProgress.getComponent(1);
                        ProgressBar progressBar = (ProgressBar) progressLayout.getComponent(0);
                        progressBar.addStyleName("import-progress-done");
                        Label filenameLabel = (Label) fileProgress.getComponent(0);
//                        Notification.show("Upload Completed " + filenameLabel.getValue());
                        UI.getCurrent().setPollInterval(-1);
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
        //upload.addStyleName("custom-upload-style");
        upload.addSucceededListener(this);
        upload.addStartedListener(this);
        upload.setImmediateMode(true);
        //upload.setButtonStyleName("custom-upload-button-style");
        upload.setButtonCaption("Browse File");
        upload.setId("VLbsUpload");
    }

    @Override
    public void uploadFailed(Upload.FailedEvent event) {

        //Notification.show("Upload Failed" + " " + event.getReason());
        UI.getCurrent().setPollInterval(-1);
    }

    @Override
    public void uploadFinished(Upload.FinishedEvent event) {

        System.out.println("upload finished");
        if(progressListener != null) {
            progressListener.remove();
            progressListener = null;
        }

    }


    @Override
    public void uploadStarted(Upload.StartedEvent event) {

        System.out.println("upload evet:" + event.getFilename());
        if(!StringUtils.isEmpty(event.getFilename())) {

            System.out.println("New progress");
            FileUploadProgress fileProgress = new FileUploadProgress(event.getFilename());
            HorizontalLayout iconsLayout = (HorizontalLayout) fileProgress.getComponent(2);
            Button deleteButton = (Button) iconsLayout.getComponent(0);
            Button fileUploadedButton = (Button) iconsLayout.getComponent(1);

            if(fileUploadedButton != null){
                fileUploadedButton.setVisible(false);
            }

            //Button deleteButton = (Button) fileProgress.getComponent(2);
            deleteButton.addClickListener(e -> {
                upload.interruptUpload();
                fileProgress.setVisible(false);
                this.tempFile.delete();
            });

            if(progressListener != null)
                progressListener.remove();

            progressListener = event.getUpload().addProgressListener(new Upload.ProgressListener() {
                @Override
                public void updateProgress(long readBytes, long contentLength) {
                    fileProgress.setValue(readBytes / (float) contentLength);
                }
            });

            event.getUpload().addSucceededListener(new Upload.SucceededListener(){

                @Override
                public void uploadSucceeded(Upload.SucceededEvent finishedEvent) {

                    HorizontalLayout progressLayout = (HorizontalLayout) fileProgress.getComponent(1);
                    ProgressBar progressBar = (ProgressBar) progressLayout.getComponent(0);

                    progressBar.addStyleName("import-progress-done");

                    Label filenameLabel = (Label) fileProgress.getComponent(0);
                    Notification.show("Upload Completed " + filenameLabel.getValue());
                    UI.getCurrent().setPollInterval(-1);


                    if(fileUploadedButton != null){
                        fileUploadedButton.setVisible(true);
                    }

                    System.out.println("upload succeed");
                }
            });

            fileArea.addComponent(fileProgress);
            System.out.println("upload started");
        }else{
            Notification.show("Select a File!");
            UI.getCurrent().setPollInterval(-1);
            upload.interruptUpload();
        }
    }

    @Override
    public void uploadSucceeded(Upload.SucceededEvent event) {

    }

    //------------------------------------------------

    class FileUploadProgress extends HorizontalLayout
    {
        private FileUploadProgress fileProgress;
        private Label filenameLabel;
        private HorizontalLayout progressLayout;
        private ProgressBar progress;
        private HorizontalLayout iconsLayout;
        private Button deleteButton;
        private Button fileUploadedButton;

        FileUploadProgress(String filename)
        {
            this.setMargin(false);
            this.setWidth("100%");

            filenameLabel = new Label(filename);
            filenameLabel.setWidth("100%");

            progress = new ProgressBar();
            progressLayout = new HorizontalLayout();

            progressLayout.addComponent(progress);
            progressLayout.setWidth(120, Unit.PIXELS);
            progress.setWidth(100, Unit.PERCENTAGE);

            iconsLayout = new HorizontalLayout();

            deleteButton = new Button();
            deleteButton.setWidth(15, Unit.PIXELS);
            deleteButton.addStyleName("color-red");
            deleteButton.addStyleName(ValoTheme.BUTTON_BORDERLESS);
            deleteButton.addStyleName(ValoTheme.BUTTON_ICON_ONLY);
            deleteButton.setIcon(VaadinIcons.CLOSE_CIRCLE);

            fileUploadedButton = new Button();
            fileUploadedButton.setWidth(15, Unit.PIXELS);
            fileUploadedButton.addStyleName("color-green");
            fileUploadedButton.addStyleName(ValoTheme.BUTTON_BORDERLESS);
            fileUploadedButton.addStyleName(ValoTheme.BUTTON_ICON_ONLY);
            fileUploadedButton.setIcon(VaadinIcons.CHECK_CIRCLE);
            fileUploadedButton.setVisible(false);
            fileUploadedButton.setEnabled(false);

            iconsLayout.addComponents(deleteButton, fileUploadedButton);


            this.addComponents(filenameLabel, progressLayout, iconsLayout);
            this.setExpandRatio(filenameLabel, 12f);
            this.setComponentAlignment(filenameLabel, Alignment.TOP_CENTER);
            this.setComponentAlignment(progressLayout, Alignment.BOTTOM_LEFT);
            this.setComponentAlignment(iconsLayout, Alignment.TOP_CENTER);
//            this.setComponentAlignment(deleteButton, Alignment.TOP_CENTER);
        }

        public void setValue(float value)
        {
            progress.setValue(value);
        }

    }


}