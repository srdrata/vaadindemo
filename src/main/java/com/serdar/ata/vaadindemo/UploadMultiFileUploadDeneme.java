package com.serdar.ata.vaadindemo;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.StreamVariable;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.dnd.FileDropTarget;
import com.vaadin.ui.themes.ValoTheme;
import com.wcs.wcslib.vaadin.widget.multifileupload.ui.MultiFileUpload;
import com.wcs.wcslib.vaadin.widget.multifileupload.ui.UploadFinishedHandler;
import com.wcs.wcslib.vaadin.widget.multifileupload.ui.UploadStatePanel;
import com.wcs.wcslib.vaadin.widget.multifileupload.ui.UploadStateWindow;
import org.vaadin.viritin.label.MLabel;
import org.vaadin.viritin.layouts.MVerticalLayout;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Serdar.Ata
 * Created by Serdar.Ata  on 28/06/2019.
 */
public class UploadMultiFileUploadDeneme extends Window {


    private VerticalLayout mainLayout;
    private UploadStateWindow uploadStateWindow = new UploadStateWindow();
    private UploadFinishedHandler uploadFinishedHandler;
    private double uploadSpeed = 100; ///
    private boolean uploadFieldsEnabled = true;
    private boolean multiple = true;
    private  SlowUpload slowUpload ;
    int maxFileSize = 5242880; //5 MB
    int FILE_COUNT = 10;

    private VerticalLayout dropLayout;
    private Panel dropPanel;

    public UploadMultiFileUploadDeneme(){
        super("Upload Multi File");
        mainLayout = new VerticalLayout();
        mainLayout.setMargin(true);
        setPosition(20, 150);
        setModal(true);
        setWidth("400px");
        setHeight("400px");
        setContent(mainLayout);
        slowUpload = null;

        initSlowUpload();
        mainLayout.addComponent(slowUpload);

        initDropArea(slowUpload);

        MVerticalLayout components = new MVerticalLayout().withUndefinedSize().withDefaultComponentAlignment(Alignment.MIDDLE_CENTER).withSpacing(true).withFullWidth().with(
                new MLabel(VaadinIcons.UPLOAD.getHtml()).withContentMode(ContentMode.HTML).withStyleName(ValoTheme.LABEL_COLORED, "upload-icon"),
                new MLabel("Dosyayı sürükle-bırak").withStyleName(ValoTheme.LABEL_LARGE),
                new MLabel("Ya da").withStyleName(ValoTheme.LABEL_LARGE),
                slowUpload
        );

        dropLayout.addComponent(components);
        dropLayout.setComponentAlignment(components, Alignment.MIDDLE_CENTER);
        mainLayout.addComponent(dropLayout);

    }


    public void initSlowUpload(){
        slowUpload = new SlowUpload(uploadFinishedHandler, uploadStateWindow, multiple);
        slowUpload.setCaption("Multi Selection");
        slowUpload.setMaxFileSize(maxFileSize);
        slowUpload.setPanelCaption("Multi Select Upload");
        slowUpload.getSmartUpload().setUploadButtonCaptions("Upload Single File", "Upload Multi Files");/// Upload button caption
        slowUpload.setMaxFileCount(FILE_COUNT); ///
    }

    public void initDropArea(SlowUpload slowUpload){
        dropLayout = new VerticalLayout();
        dropLayout.setSizeFull();

        dropPanel = new Panel(dropLayout);
        dropPanel.setSizeFull();
        dropPanel.setStyleName(ValoTheme.PANEL_WELL);

//        DragAndDropWrapper dragAndDropWrapper = slowUpload.createDropComponent(dropPanel);
//        dragAndDropWrapper.setSizeUndefined();
        FileDropTarget target = new FileDropTarget<>(dropLayout, event -> {
            System.out.println(event.getFiles().toArray().length);
        });

    }


    private class SlowUpload extends MultiFileUpload {

        public SlowUpload(UploadFinishedHandler handler, UploadStateWindow uploadStateWindow) {
            super(handler, uploadStateWindow, true);
        }

        public SlowUpload(UploadFinishedHandler handler, UploadStateWindow uploadStateWindow, boolean multiple) {
            super(handler, uploadStateWindow, multiple);
        }

        @Override
        protected UploadStatePanel createStatePanel(UploadStateWindow uploadStateWindow) {
            return new SlowUploadStatePanel(uploadStateWindow);
        }
    }

    private class SlowUploadStatePanel extends UploadStatePanel {

        public SlowUploadStatePanel(UploadStateWindow window) {
            super(window);
        }

        @Override
        public void onProgress(StreamVariable.StreamingProgressEvent event) {
            try {
                Thread.sleep((int) uploadSpeed);
            } catch (InterruptedException ex) {
                System.out.println(ex.getMessage());
            }
            super.onProgress(event);
        }
    }

}



