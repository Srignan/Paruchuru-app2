package ucf.assignments;
/*
 *  UCF COP3330 Fall 2021 Application Assignment 2 Solution
 *  Copyright 2021 Srignan Paruchuru
 */
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.Objects;

public class InventoryTracker extends Application
{
    private static Stage mainWindow;

    public static Stage getMainWindow()
    {
        return mainWindow;
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException
    {
        mainWindow = primaryStage;
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("InventoryGUI.fxml")));
        Scene scene = new Scene(root);

        primaryStage.setTitle("Inventory App");
        primaryStage.setResizable(false);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
