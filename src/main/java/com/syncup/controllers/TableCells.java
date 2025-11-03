package com.syncup.controllers;

import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import com.syncup.models.Cancion;

public class TableCells {
    public static Callback<TableColumn<Cancion, Void>, TableCell<Cancion, Void>> coverCellFactory(){
        return col -> new TableCell<>(){
            private final ImageView iv = new ImageView();
            private final HBox box = new HBox(iv);
            { iv.setFitWidth(48); iv.setFitHeight(48); iv.setPreserveRatio(false); box.setAlignment(Pos.CENTER); }
            @Override protected void updateItem(Void item, boolean empty){
                super.updateItem(item, empty);
                if(empty){ setGraphic(null); }
                else{
                    Cancion c = getTableView().getItems().get(getIndex());
                    String url=c.getCoverUrl();
                    Image img;
                    if(url!=null && !url.isEmpty()) img=new Image(url,48,48,false,true,true);
                    else img=new Image(getClass().getResourceAsStream("/images/cover-placeholder.png"),48,48,false,true);
                    iv.setImage(img);
                    setGraphic(box);
                }
            }
        };
    }
}
