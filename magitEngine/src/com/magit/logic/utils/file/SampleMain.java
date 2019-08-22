package com.magit.logic.utils.file;
import com.fxgraph.edges.Edge;
import com.fxgraph.graph.ICell;
import com.fxgraph.graph.Model;
import com.fxgraph.graph.PannableCanvas;
import com.magit.logic.system.objects.FileItem;
import com.magit.logic.visual.layout.CommitTreeLayout;
import com.magit.logic.visual.node.CommitNode;


import com.fxgraph.graph.Graph;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;


import java.io.FileOutputStream;
import java.net.URL;
import java.nio.file.Paths;
import java.util.zip.GZIPOutputStream;

public class SampleMain extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FileHandler.writeNewFolder("c:/dzipppppppppped");
        String a = FileHandler.readFile("C:\\Users\\guybb\\Downloads\\gzipme\\82678fcdc6a3c65bd2745d17c8e4c8f2a2162d94");
        try (FileOutputStream fos = new FileOutputStream(Paths.get("c:/dzipppppppppped/82678fcdc6a3c65bd2745d17c8e4c8f2a2162d94").toString());
             GZIPOutputStream gzipOutputStream = new GZIPOutputStream(fos)) {
            gzipOutputStream.write(a.getBytes());
            gzipOutputStream.flush();
        }
        Graph tree = new Graph();
        createCommits(tree);

        FXMLLoader fxmlLoader = new FXMLLoader();
        URL url = getClass().getResource("/com/magit/resources/main.fxml");
        fxmlLoader.setLocation(url);
        GridPane root = fxmlLoader.load(url.openStream());

        final Scene scene = new Scene(root, 700, 400);

        ScrollPane scrollPane = (ScrollPane) scene.lookup("#scrollpaneContainer");
        PannableCanvas canvas = tree.getCanvas();
        //canvas.setPrefWidth(100);
        //canvas.setPrefHeight(100);
        scrollPane.setContent(canvas);

        Button button = (Button) scene.lookup("#pannableButton");
        button.setOnAction(e -> addMoreCommits(tree));

        primaryStage.setScene(scene);
        primaryStage.show();

        Platform.runLater(() -> {
            tree.getUseViewportGestures().set(false);
            tree.getUseNodeGestures().set(false);
        });

    }

    private void createCommits(Graph graph) {
        final Model model = graph.getModel();

        graph.beginUpdate();
        String s = new String("30.10.2019 | 11:36:55");
        ICell c1 = new CommitNode("20.07.2019 | 22:36:57", "Menash", "initial commit");
        ICell c2 = new CommitNode("21.07.2019 | 22:36:57", "Moyshe Ufnik", "developing some feature");
        ICell c3 = new CommitNode("21.08.2019 | 22:36:57", "Old Majesty, The FU*!@N Queen of england", "A very long commit that aims to see if and where the line will be cut and how it will look a like... very Interesting");
        ICell c4 = new CommitNode("20.09.2019 | 13:33:57", "el professore", "yet another commit");
        ICell c5 = new CommitNode("30.10.2019 | 11:36:54", "bella chao", "merge commit of 'yet another commit' and other commit");
        ICell c6 = new CommitNode(s, "bella chaoa", "merge commit of 'yet another commit' and other commita");
        ICell c7 = new CommitNode("30.10.2019 | 11:37:59", "bella chaoa", "merge commit of 'yet another commit' and other commita");
        model.addCell(c1);
        model.addCell(c2);
        model.addCell(c3);
        model.addCell(c4);
        model.addCell(c5);
        model.addCell(c6);
        model.addCell(c7);
        final Edge edgeC12 = new Edge(c2, c1);
        model.addEdge(edgeC12);

        final Edge edgeC23 = new Edge(c3, c2);
        model.addEdge(edgeC23);

        final Edge edgeC45 = new Edge(c4, c3);
        model.addEdge(edgeC45);

        final Edge edgeC13 = new Edge(c5, c3);
        model.addEdge(edgeC13);

        final Edge edgeC35 = new Edge(c4, c2);
        model.addEdge(edgeC35);
//
        final Edge edgeC= new Edge(c5, c3);
        model.addEdge(edgeC);
        final Edge edgecC= new Edge(c6, c5);
        model.addEdge(edgecC);

        final Edge edgeCa = new Edge(c7, c4);
        model.addEdge(edgeCa);
        final Edge edgeCaa = new Edge(c7, c6);
        model.addEdge(edgeCaa);

       // final Edge edgeCccc = new Edge(c7, c1);
       // model.addEdge(edgeCccc);
        graph.endUpdate();
        graph.layout(new CommitTreeLayout());

    }

    private void addMoreCommits(Graph graph) {
        final Model model = graph.getModel();
        //graph.beginUpdate();
        ICell lastCell = model.getAllCells().get(4);

        ICell c1 = new CommitNode("20.07.2020 | 22:36:57", "Menash", "initial commit");
        ICell c2 = new CommitNode("21.07.2020 | 22:36:57", "Moyshe Ufnik", "developing some feature");
        ICell c3 = new CommitNode("20.08.2020 | 22:36:57", "Old Majesty, The FU*!@N Queen of england", "A very long commit that aims to see if and where the line will be cut and how it will look a like... very Interesting");
        ICell c4 = new CommitNode("20.09.2020 | 13:33:57", "el professore", "yet another commit");
        ICell c5 = new CommitNode("30.10.2020 | 11:36:54", "bella chao", "merge commit of 'yet another commit' and other commit");

        model.addCell(c1);
        model.addCell(c2);
        model.addCell(c3);
        model.addCell(c4);
        model.addCell(c5);

        final Edge edgeLastCellC1 = new Edge(lastCell, c1);
        model.addEdge(edgeLastCellC1);

        final Edge edgeC12 = new Edge(c1, c2);
        model.addEdge(edgeC12);

        final Edge edgeC23 = new Edge(c2, c4);
        model.addEdge(edgeC23);

        final Edge edgeC45 = new Edge(c4, c5);
        model.addEdge(edgeC45);

        final Edge edgeC13 = new Edge(c1, c3);
        model.addEdge(edgeC13);

        final Edge edgeC35 = new Edge(c3, c5);
        model.addEdge(edgeC35);

        graph.endUpdate();

        graph.layout(new CommitTreeLayout());
    }

}
