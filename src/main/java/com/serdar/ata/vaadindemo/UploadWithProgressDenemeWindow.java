package com.serdar.ata.vaadindemo;

import com.vaadin.ui.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.LinkedList;

/**
 * @author Serdar.Ata
 * Created by Serdar.Ata  on 28/06/2019.
 */

//https://vaadin.com/attachment/65ba1596-4d70-470f-8328-c8de53367c7b/UploadFileWithProgress.java
public class UploadWithProgressDenemeWindow extends Window implements Upload.Receiver, Upload.ProgressListener, Upload.StartedListener, Upload.FinishedListener, Upload.SucceededListener, Upload.FailedListener {

    private Upload upload;
    private String directoryPath;
    private String fileName;
    private String mimeType;
    private File file;
    private ByteArrayOutputStream fileDataByteArrayOutputStream;
    private byte[] fileData;
    private int maxSizeInBytes;
    private ProgressBar progressIndicator;
    private Label fileNameLabel;
    private boolean isCanceled = false;
    private boolean isNotAllowedMimeType = false;
    private boolean isTooBig = false;
    private Integer contentLength;
    private Button cancelButton;
    private HorizontalLayout processingLayout;
    private LinkedList<String> allowedMimeTypes;
    private VerticalLayout mainLayout;


    public UploadWithProgressDenemeWindow(){
        super("Upload with Progress Deneme");
        mainLayout = new VerticalLayout();


        setPosition(20, 150);
        setWidth("400px");
        setHeight("400px");
        setModal(true);

        maxSizeInBytes = maxSizeInBytes < 1 ? Integer.MAX_VALUE : maxSizeInBytes;
        System.out.println(maxSizeInBytes);




        upload = new Upload("Upload deneme",null);
        upload.setImmediateMode(true);
        upload.setButtonCaption("Upload File");
        upload.setReceiver(this);
        upload.addProgressListener(this::updateProgress);
        upload.addSucceededListener(this::uploadSucceeded);
        upload.addFailedListener(this::uploadFailed);
        upload.addFinishedListener(this::uploadFinished);
        upload.addStartedListener(this::uploadStarted);

        processingLayout = new HorizontalLayout();
        processingLayout.setMargin(true);
        processingLayout.setSpacing(true);
        processingLayout.setVisible(false);

        progressIndicator = new ProgressBar();
        progressIndicator.setEnabled(false);
        fileNameLabel = new Label();

        processingLayout.addComponents(fileNameLabel, progressIndicator);

        cancelButton = new Button("Cancel");

        cancelButton.addClickListener(event -> {
            System.out.println("cancelButton.buttonClick()");
            isCanceled = true;
            upload.interruptUpload();
        });

        cancelButton.setDisableOnClick(true);
        processingLayout.addComponent(cancelButton);


        mainLayout.setMargin(true);
        mainLayout.addComponents(upload, processingLayout);
        mainLayout.setSizeFull();
        setContent(mainLayout);



    }





    boolean isAllowedMimeType(String mimeType) {
        if ( allowedMimeTypes == null )
            return true;
        for( String allowMimeType : allowedMimeTypes ) {
            if ( allowMimeType.equals(mimeType) ) {
                return true;
            }
        }
        return false;
    }

    void enableProgressIndicator() {
        progressIndicator.setValue(0.0f);
        progressIndicator.setEnabled(true);
        processingLayout.setVisible(true);
    }

    void disableProgressIndicator() {
        progressIndicator.setValue(0.0f);
        progressIndicator.setEnabled(false);
        processingLayout.setVisible(false);
    }

    @Override
    public OutputStream receiveUpload(String filename, String mimeType) {
        resetUpload();
        this.fileName = filename;
        this.mimeType = mimeType;
        if ( ! isAllowedMimeType(mimeType) ) {
            isNotAllowedMimeType = true;
            upload.interruptUpload();

        }
        return receiveUploadToBuffer(filename,mimeType);
       // return hasDirectoryPath() ? receiveUploadToFile(filename,mimeType) : receiveUploadToBuffer(filename,mimeType);
    }

    OutputStream receiveUploadToFile(String filename, String mimeType) {
        File directoryDir = new File(directoryPath);
        if ( ! directoryDir.exists() ) {
            directoryDir.mkdirs();
        }

        this.fileName = filename;
        file = new File(directoryDir, filename);
        fileData = null;

        try {
            return new FileOutputStream(file);
        } catch (final java.io.FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    OutputStream receiveUploadToBuffer(String filename, String mimeType) {
        try {
            fileDataByteArrayOutputStream = new ByteArrayOutputStream();
            return fileDataByteArrayOutputStream;
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateProgress(long readBytes, long contentLength) {
        this.contentLength = new Long(contentLength).intValue();
        if (isNotAllowedMimeType || readBytes > maxSizeInBytes || contentLength > maxSizeInBytes) {
            upload.interruptUpload();
            return;
        }

        progressIndicator.setValue( new Float((float)readBytes / (float)contentLength) );
    }

    @Override
    public void uploadStarted(Upload.StartedEvent event) {
        if ( ! isAllowedMimeType(event.getMIMEType()) ) {
            System.out.println("uploadedStarted() - INVALID MIME TYPE: " + event.getMIMEType());
            isNotAllowedMimeType = true;
            upload.interruptUpload();
            return;
        }

        if( event.getContentLength() > maxSizeInBytes ) {
            System.out.println("uploadedStarted() - TOO BIG; content-length: " + event.getContentLength() + "; maxSize: " + maxSizeInBytes);
            isTooBig = true;
            upload.interruptUpload();
            return;
        }

        enableProgressIndicator();
        fileNameLabel.setValue(event.getFilename());
    }

    @Override
    public void uploadFinished(Upload.FinishedEvent event) {
        disableProgressIndicator();
    }

    @Override
    public void uploadFailed(Upload.FailedEvent event) {
        isTooBig = false;
        if (contentLength != null && contentLength > maxSizeInBytes) {
            isTooBig = true;
        } else if (isNotAllowedMimeType || isCanceled) {
            // Nothing to do here
        } else {
            System.out.println( "UploadFileWithProgress.uploadFailed()");
        }

//        afterUploadFailed(fileName, isCanceled, isNotAllowedMimeType, isTooBig, contentLength == null ? 0 : contentLength, maxSizeInBytes);

        resetUpload();
    }

    /**
     * Override this method to handle the successful upload.
     */
    @Override
    public void uploadSucceeded(Upload.SucceededEvent event) {
        if ( fileDataByteArrayOutputStream != null ) {
            try {
                fileDataByteArrayOutputStream.flush();
                fileData = fileDataByteArrayOutputStream.toByteArray();
                fileDataByteArrayOutputStream.close(); // presume the upload actually closes this
            } catch (Exception e) {}
            finally {
                fileDataByteArrayOutputStream = null;
            }
        }
        if ( isNotAllowedMimeType ) { // small files can be fully uploaded before we detect an invalid mime type
            System.out.println("uploadSucceeded() - INVALID MIME TYPE detected after successful upload: " + mimeType);
            //afterUploadFailed(fileName,isCanceled,isNotAllowedMimeType,false,contentLength,maxSizeInBytes);
            resetUpload();
        }
    }

    protected void showNotification(String caption, String message) {
       Notification.show(caption +  message);
    }

    public String getDirectoryPath() {
        return directoryPath;
    }


    public File getFile() {
        return file;
    }
    public boolean hasFile() {
        return file != null;
    }

    public byte[] getFileData() {
        if ( hasFile() && ! hasFileData() ) {
            return null;  // TODO: return the bytes in the file
        }
        return fileData;
    }
    public boolean hasFileData() {
        return fileData != null;
    }

    public String getFileName() {
        return fileName;
    }
    public String getMimeType() {
        return mimeType;
    }
    public int getContentLength() {
        return contentLength;
    }
    public boolean hasContentLength() {
        return contentLength >= 0;
    }


    protected void resetUpload() {
        if ( hasFile() ) {
            try {
                file.delete();
            } catch (Exception e) {}
            finally {
                file = null;
            }
        }
        if ( fileDataByteArrayOutputStream != null ) {
            try {
                fileDataByteArrayOutputStream.close();
            } catch (Exception e) {}
            finally {
                fileDataByteArrayOutputStream = null;
            }
        }
        fileData = null;
        mimeType = null;
        fileName = null;
        isCanceled = isNotAllowedMimeType = isTooBig = false;
        contentLength = -1;
    }

    /**
     * Override this method to handle the successful upload.
     */
   // public abstract void afterUploadSucceeded();

    /**
     * Override this method to handle the failure upload
     */
//   public abstract void afterUploadFailed(String fileName, boolean wasCanceled, boolean wasInvalidMimeType, boolean wasTooBig, int contentLength, int maxSize);
}