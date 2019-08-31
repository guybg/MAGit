package com.magit.controllers;

import com.magit.gui.DragResizer;
import com.magit.gui.ResizeHelper;
import com.magit.logic.enums.FileType;
import com.magit.logic.system.objects.FileItem;
import com.magit.logic.system.objects.FileItemInfo;
import com.magit.logic.system.objects.Tree;
import com.magit.logic.utils.digest.Sha1;
import com.sun.rowset.internal.Row;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.RowConstraints;
import javafx.stage.Stage;

import java.util.*;

public class TableScreenController {
    private ObservableList<FileItemInfo> itemsInfo;
    @FXML
    private TableView<FileItemInfo> tableView;
    @FXML
    private TextArea contentTextArea;


    public void init(ObservableList<FileItemInfo> items) {
        itemsInfo = items;
        tableView.getFocusModel();
        TableColumn fileNameCol = new TableColumn("File Name");
        fileNameCol.setCellValueFactory(
                new PropertyValueFactory<FileItemInfo,String>("fileName")
        );
        TableColumn fileLocationCol = new TableColumn("File Location");
        fileLocationCol.setCellValueFactory(
                new PropertyValueFactory<FileItemInfo,String>("fileLocation")
        );
        TableColumn fileTypeCol = new TableColumn("File Type");
        fileTypeCol.setCellValueFactory(
                new PropertyValueFactory<FileItemInfo,String>("fileType")
        );
        TableColumn fileSha1Col = new TableColumn("File Sha1");
        fileSha1Col.setCellValueFactory(
                new PropertyValueFactory<FileItemInfo,String>("fileSha1")
        );
        TableColumn fileLastUpdaterCol = new TableColumn("Last Updater");
        fileLastUpdaterCol.setCellValueFactory(
                new PropertyValueFactory<FileItemInfo,String>("fileLastUpdater")
        );
        TableColumn fileLastModifiedCol = new TableColumn("Last Modified");
        fileLastModifiedCol.setCellValueFactory(
                new PropertyValueFactory<FileItemInfo,String>("fileLastModified")
        );
        TableColumn fileContentCol = new TableColumn("Last Modified");
        fileContentCol.setCellValueFactory(
                new PropertyValueFactory<FileItemInfo,String>("fileLastModified")
        );
        fileContentCol.setVisible(false);
        tableView.getColumns().addAll(fileNameCol,fileTypeCol,fileSha1Col,fileLastUpdaterCol,fileLastModifiedCol,fileLocationCol, fileContentCol);
        tableView.getItems().addAll(items);

        tableView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<FileItemInfo>() {
            @Override
            public void changed(ObservableValue<? extends FileItemInfo> observable, FileItemInfo oldValue, FileItemInfo newValue) {
                contentTextArea.setText(tableView.getSelectionModel().getSelectedItem().getFileContent());
            }
        });
    }
}
