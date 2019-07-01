package com.serdar.ata.vaadindemo;

import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.server.Page;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.*;
import com.vaadin.ui.renderers.ProgressBarRenderer;
import com.vaadin.ui.renderers.TextRenderer;
import elemental.json.JsonValue;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Serdar.Ata
 * Created by Serdar.Ata  on 26/06/2019.
 */
public class UploadProgressPopup extends Window {
    private Collection<FileInfo> fileInfoCollection;
    private Map<String, FileInfo> fileNamefileInfoMap = new HashMap<>();
    private final UploadProgressLayout content;


    public UploadProgressPopup(Collection<FileInfo> fileInfoCollection) {
        this.fileInfoCollection = fileInfoCollection == null ? Collections.emptyList() : fileInfoCollection;
        setPosition(Page.getCurrent().getBrowserWindowWidth() - 310, Page.getCurrent().getBrowserWindowHeight() - 160);
        setModal(true);
        setClosable(false);
        setWidth(50, Sizeable.Unit.PERCENTAGE);
        setHeight(50, Sizeable.Unit.PERCENTAGE);
        this.fileInfoCollection.forEach(fileInfo -> fileNamefileInfoMap.put(fileInfo.getFileName(), fileInfo));
        content = new UploadProgressLayout();
        setContent(content);
    }

    public void update(String fileName, double value) {
        FileInfo fileInfo = fileNamefileInfoMap.get(fileName);
        fileInfo.setProgress(value);
        content.refresh(fileInfo);
    }

    public void show() {
        UI.getCurrent().addWindow(this);
    }


    private class UploadProgressLayout extends VerticalLayout {
        private Grid<FileInfo> fileInfoGrid;

        private UploadProgressLayout() {
            setMargin(false);
            setSpacing(false);
            setSizeFull();

            fileInfoGrid = new Grid<>(FileInfo.class);
            fileInfoGrid.getColumn("fileName").setCaption("File Name");
            fileInfoGrid.getColumn("progress").setCaption("Progress");
            fileInfoGrid.addColumn(FileInfo::getProgress
                    , new ProgressBarRenderer() {
                        @Override
                        public JsonValue encode(Double value) {
                            return super.encode(value);
                        }
                    });
            Grid.Column<FileInfo, ?> fileLength = fileInfoGrid.getColumn("fileLength");
            fileLength.setRenderer(new TextRenderer() {
                @Override
                public JsonValue encode(Object value) {
                    long contentLength = (long) value;
                    String length;
                    if (contentLength > 1000000000) {
                        length = (contentLength / 1000000000) + " Gb";
                    } else if (contentLength > 1000000) {
                        length = (contentLength / 1000000) + " Mb";
                    } else if (contentLength > 1000) {
                        length = (contentLength / 1000) + " Kb";
                    } else {
                        length = contentLength + "B";
                    }
                    return super.encode(length);
                }
            }).setCaption("File Length");

            fileInfoGrid.setDataProvider(new FileInfoDataProvider(fileInfoCollection));
            Slider progressEditor = new Slider();
            progressEditor.setWidth(100.0f, Unit.PERCENTAGE);
            progressEditor.setMax(1.0);
            progressEditor.setMin(0.0);


            fileInfoGrid.setSizeFull();
            addComponent(fileInfoGrid);

            Button closeButton = new Button("Close");
            addComponent(closeButton);
            closeButton.addClickListener(clickEvent-> {
                close();
            });
        }

        void refresh(FileInfo fileInfo) {
            fileInfoGrid.getDataProvider().refreshItem(fileInfo);
        }

        public class FileInfoDataProvider extends ListDataProvider<FileInfo> {

            /**
             * Constructs a new ListDataProvider.
             * <p>
             * No protective copy is made of the list, and changes in the provided
             * backing Collection will be visible via this data provider. The caller
             * should copy the list if necessary.
             *
             * @param items the initial data, not null
             */
            public FileInfoDataProvider(Collection<FileInfo> items) {
                super(items);
            }

            @Override
            public Object getId(FileInfo item) {
                return item.getFileName();
            }
        }
    }

    public static class FileInfo {
        private String fileName;
        private long fileLength;
        private double progress;

        public FileInfo(String fileName, long fileLength) {
            this.fileName = fileName;
            this.fileLength = fileLength;
            this.progress = 0;
        }

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public long getFileLength() {
            return fileLength;
        }

        public void setFileLength(long fileLength) {
            this.fileLength = fileLength;
        }

        public double getProgress() {
            return progress;
        }

        public void setProgress(double progress) {
            this.progress = progress;
        }
    }
}