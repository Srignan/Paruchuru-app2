package ucf.assignments;
/*
 *  UCF COP3330 Fall 2021 Application Assignment 2 Solution
 *  Copyright 2021 Srignan Paruchuru
 */

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.*;
import java.util.*;
import java.util.function.Predicate;

import javafx.scene.control.Alert.AlertType;


import static ucf.assignments.LoadSave.Type.*;

public class Inventory_GUI_Controller
{
    @FXML
    private SplitPane mainPane;
    @FXML
    private TextField nameText;
    @FXML
    private TextField moneyText;
    @FXML
    private TextField serialText;
    @FXML
    private TextField searchField;
    @FXML
    private Button addItemButton;
    @FXML
    private Button updateButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Button cancelButton;
    @FXML
    private MenuItem TSVItem;
    @FXML
    private MenuItem HTMLItem;
    @FXML
    private MenuItem JSONItem;
    @FXML
    private Label errorLabel;
    @FXML
    private TableView<Inventory> tableview;
    @FXML
    private TableColumn<Inventory, String> item_value;
    @FXML
    private TableColumn<Inventory, String> item_serial;
    @FXML
    private TableColumn<Inventory, String> item_name;


    public ObservableList<Inventory> items = FXCollections.observableArrayList();

    public int index;
    public boolean isopened = false;
    public String fname;
    public String absolutePath;

    private String name;


    FilteredList<Inventory> filteredData = new FilteredList<>(items, e -> true);

    @FXML
    public void initialize()
    {

        toggleButtons(false);
        item_value.setCellValueFactory(new PropertyValueFactory<>("dollars"));

        item_serial.setCellValueFactory(new PropertyValueFactory<>("serial_number"));

        item_name.setCellValueFactory(new PropertyValueFactory<>("name"));


        tableview.setOnMouseClicked((MouseEvent event) -> {
            if (event.getClickCount() > 1) {
                if(tableview.getSelectionModel().getSelectedItem() != null)
                {
                    updateButton.setDisable(false);
                    addItemButton.setDisable(true);
                    index = tableview.getSelectionModel().getSelectedIndex();

                    String name = items.get(index).getName();
                    nameText.setText(name);

                    String serial = items.get(index).getSerial_number();
                    serialText.setText(serial);

                    double money = items.get(index).getValue();
                    String money_str = String.valueOf(money);
                    moneyText.setText(money_str);
                }
            }


        });


        tableview.getSelectionModel().clearSelection();


    }

    @FXML
    public void saveClicked(ActionEvent actionEvent)
    {
        Object src = actionEvent.getSource();
        errorLabel.setText("");
        if (src == TSVItem)
        {
            Save_as(TSV);
        }

        else if (src == HTMLItem)
        {
            Save_as(HTML);
        }

        else if (src == JSONItem)
        {
            Save_as(JSON);
        }
    }

    private void Save_as(LoadSave.Type type)
    {
        FileChooser chooser = new FileChooser();

        FileChooser.ExtensionFilter filter = getExtensionFilter(type);
        chooser.getExtensionFilters().add(filter);
        File selectedFile = chooser.showSaveDialog(InventoryTracker.getMainWindow());

        ArrayList<Inventory> list = new ArrayList<>(items);
        AppData data = new AppData(list);

        if (selectedFile != null)
        {
            try
            {
                isopened = true;
                fname = selectedFile.getName();
                absolutePath = selectedFile.getAbsolutePath();

                LoadSave.Save_As(selectedFile, type, data.getList());

            }
            catch (Exception e)
            {
                showAlert("Save Error", e.getMessage());
            }
        }
    }

    @FXML
    public void saveOpenedClicked() throws IOException
    {
        errorLabel.setText("");

        if(isopened)
        {

            new FileOutputStream(absolutePath).close();

            if(fname.endsWith(".txt"))
            {
                try(FileWriter writer = new FileWriter(absolutePath))
                {
                    ArrayList<Inventory> list = new ArrayList<>(items);
                    AppData data = new AppData(list);
                    LoadSave.Save_As_TSV(writer,data.getList());
                    printError("File Saved!");
                }
            }

            else if(fname.endsWith(".html"))
            {
                try(FileWriter writer = new FileWriter(absolutePath))
                {
                    ArrayList<Inventory> list = new ArrayList<>(items);
                    AppData data = new AppData(list);
                    LoadSave.Save_As_HTML(writer,data.getList());
                    printError("File Saved!");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            else
            {
                try(FileWriter writer = new FileWriter(absolutePath))
                {
                    ArrayList<Inventory> list = new ArrayList<>(items);
                    AppData data = new AppData(list);
                    LoadSave.Save_As_JSON(writer,data.getList());
                    printError("File Saved!");
                }

            }
        }

        else
        {
            if(items.isEmpty()) {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setHeaderText("Error");
                alert.setContentText("Please Enter Some Data And Then Save It As A TSV/HTML/JSON File First");
                alert.show();
            }

            else{
                Alert alert = new Alert(AlertType.ERROR);
                alert.setHeaderText("Error");
                alert.setContentText("Please Save The Data As A TSV/HTML/JSON File First");
                alert.show();
            }
        }
    }

    @FXML
    public void openClicked()
    {
        errorLabel.setText("");
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().addAll(getExtensionFilter(TSV), getExtensionFilter(HTML), getExtensionFilter(JSON));
        File selectedFile = fc.showOpenDialog(new Stage());

        if(selectedFile != null)
        {

            try
            {
                isopened = true;
                fname = selectedFile.getName();
                absolutePath = selectedFile.getAbsolutePath();

                ArrayList<Inventory> list = LoadSave.Open(selectedFile);

                AppData data = new AppData(list);
                items.setAll(data.getList());
                tableview.setItems(items);

            }

            catch (Exception e)
            {
                e.printStackTrace();
                showAlert("File Open Error", e.getMessage());
            }
        }


    }

    @FXML
    void addNewItem()
    {
        if(addItemValidate())
        {
            addItemCommit();
        }
    }

    private boolean addItemValidate() {

        if( nameText.getText().equals(""))
        {
            printError("Cannot create an empty inventory item");
            return false;
        }
        else if( moneyText.getText().equals(""))
        {
            printError("Cannot create an inventory item with no monetary value");
            return false;
        }
        else if(serialText.getText().equals(""))
        {
            printError("Cannot create an inventory item with no serial number");
            return false;
        }


        else if(!isDouble(moneyText.getText()))
        {
            printError("Invalid Monetary Value");
            return false;
        }

        else if(!isSerial(serialText.getText()))
        {
            printError("Invalid Serial Number");
            return false;
        }

        else if(nameText.getText().length() < 2)
        {
            printError("Please Enter More than 1 Character");
            return false;
        }

        else if(nameText.getText().length() > 256)
        {
            printError("Please Enter Less Than 257 Characters");
            return false;
        }

        if(isDuplicate())
        {
            printError("Cannot create an inventory item with duplicate serial number");
            return false;
        }

        return true;
    }

    private void addItemCommit()
    {
        double value = Double.parseDouble(moneyText.getText());
        double roundedvalue = Math.round(value * 100.0) / 100.0;

        items.add(new Inventory(roundedvalue, serialText.getText(), nameText.getText()));

        tableview.setItems(items);
        nameText.setText("");
        moneyText.setText("");
        serialText.setText("");
        errorLabel.setText("");

        toggleButtons(items.isEmpty());

    }

    private boolean isDouble(String str)
    {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static boolean isStringUpperCase(String str)
    {
        char currentCharacter;
        String specialChars = "~`!@#$%^&*()-_=+\\|[{]};:'\",<.>/?";

        for (int i = 0; i < str.length(); i++)
        {
            currentCharacter = str.charAt(i);
            if (!Character.isDigit(currentCharacter) && !specialChars.contains(String.valueOf(currentCharacter)))
            {
                if(!Character.isUpperCase(currentCharacter))
                {
                    return false;
                }

            }
        }

        return true;
    }

    private boolean isSerial(String str)
    {

        return isStringUpperCase(str) && (str.length() == 10) && str.matches("^[a-zA-Z0-9]*$");
    }

    private boolean isDuplicate()
    {
        for (Inventory item : items) {
            if (serialText.getText().equals(item.getSerial_number()))
                return true;
        }

        return false;
    }

    @FXML
    public void search()
    {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> filteredData.setPredicate((Predicate<? super Inventory>) (Inventory inventory) -> {

            if (newValue == null || newValue.isEmpty())
            {
                return true;
            }

            else // Does not match.
                if (inventory.getName().toLowerCase().contains(newValue) || inventory.getName().toUpperCase().contains(newValue))
            {
                return true; // Filter matches name.
            }
            else return inventory.getSerial_number().toLowerCase().contains(newValue) || inventory.getSerial_number().toUpperCase().contains(newValue); // Filter matches serial number.
        }));


        SortedList<Inventory> sortedData = new SortedList<>(filteredData);

        sortedData.comparatorProperty().bind(tableview.comparatorProperty());

        tableview.setItems(sortedData);

    }


    @FXML
    void deleteItem()
    {
        if(tableview.getSelectionModel().getSelectedItem() != null) {
            items.remove(tableview.getSelectionModel().getSelectedItem());
            tableview.refresh();
            tableview.getSelectionModel().clearSelection();

            nameText.setText("");
            moneyText.setText("");
            serialText.setText("");
            errorLabel.setText("");

            addItemButton.setDisable(false);
            updateButton.setDisable(false);
            cancelButton.setDisable(false);
            deleteButton.setDisable(false);
        }

        else
        {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setHeaderText("Error");
            alert.setContentText("Please Select An Item To Delete");
            alert.show();
        }
    }

    private boolean editItemValidate()
    {

        if( nameText.getText().equals(""))
        {
            printError("Cannot update an empty inventory item");
            return false;
        }
        else if( moneyText.getText().equals(""))
        {
            printError("Cannot update an inventory item with no monetary value");
            return false;
        }
        else if(serialText.getText().equals(""))
        {
            printError("Cannot update an inventory item with no serial number");
            return false;
        }


        else if(!isDouble(moneyText.getText()))
        {
            printError("Invalid Monetary Value");
            return false;
        }

        else if(!isSerial(serialText.getText()))
        {
            printError("Invalid Serial Number");
            return false;
        }

        else if(nameText.getText().length() < 2)
        {
            printError("Please Enter More than 1 Character");
            return false;
        }

        else if(nameText.getText().length() > 256)
        {
            printError("Please Enter Less Than 257 Characters");
            return false;
        }

        if(isDuplicate_New())
        {
            printError("Cannot create an inventory item with duplicate serial number");
            return false;
        }

        return true;
    }

    private boolean isDuplicate_New()
    {
        for(int i = 0; i < index; i++)
        {
            if (serialText.getText().equals(items.get(i).getSerial_number()))
                return true;
        }

        for(int i = index + 1; i < items.size(); i++)
        {
            if (serialText.getText().equals(items.get(i).getSerial_number()))
                return true;
        }

        return false;
    }

    @FXML
    public void updateItemClicked()
    {

        if(tableview.getSelectionModel().getSelectedItem() != null)
        {

            if(editItemValidate())
            {

                items.remove(index);
                double value = Double.parseDouble(moneyText.getText());
                double roundedvalue = Math.round(value * 100.0) / 100.0;
                items.add(index, new Inventory(roundedvalue, serialText.getText(), nameText.getText()));

                tableview.setItems(items);
                tableview.getSelectionModel().clearSelection();
                nameText.setText("");
                moneyText.setText("");
                serialText.setText("");
                errorLabel.setText("");


                toggleButtons(items.isEmpty());
            }


        }

        else
        {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setHeaderText("Error");
            alert.setContentText("Please Select An Item To Update");
            alert.show();
        }


    }



    @FXML
    public void cancelClicked()
    {
        tableview.getSelectionModel().clearSelection();
        nameText.setText("");
        moneyText.setText("");
        serialText.setText("");
        errorLabel.setText("");


        addItemButton.setDisable(false);
        updateButton.setDisable(false);
        cancelButton.setDisable(false);
        deleteButton.setDisable(false);

    }


    
    @FXML
    public void sortName()
    {
        items.sort((t1, t2) -> {

            if (t1.getName().compareTo(t2.getName()) > 0 || t1.getName().compareTo(t2.getName()) == 0) {
                return 1;
            }
            if (t1.getName().compareTo(t2.getName()) < 0) {
                return -1;
            }
            return 0;
        });

        tableview.setItems(items);
    }


    @FXML
    public void sortSerialNumber()
    {
        items.sort((t1, t2) -> {

            if (t1.getSerial_number().compareTo(t2.getSerial_number()) > 0 || t1.getSerial_number().compareTo(t2.getSerial_number()) == 0) {
                return 1;
            }
            if (t1.getSerial_number().compareTo(t2.getSerial_number()) < 0) {
                return -1;
            }
            return 0;
        });

        tableview.setItems(items);
    }

    @FXML
    public void sortValue()
    {
        items.sort((t1, t2) -> {

            if (t1.getValue() > t2.getValue() || t1.getValue() == t2.getValue()) {
                return 1;
            }
            if (t1.getValue() < t2.getValue()) {
                return -1;
            }
            return 0;
        });

        tableview.setItems(items);
    }

    private void toggleButtons(boolean listsEmpty)
    {
        deleteButton.setDisable(listsEmpty);
        addItemButton.setDisable(listsEmpty);
    }

    private void printError(String text)
    {

        errorLabel.setText(text);
        errorLabel.setTextFill(Color.RED);

    }

    private void showAlert(String title, String message)
    {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.show();
    }

    private FileChooser.ExtensionFilter getExtensionFilter(LoadSave.Type type)
    {
        if (type == TSV)
        {
            return new FileChooser.ExtensionFilter(type.getFilename(),type.getExtension());
        }
        else if (type == HTML)
        {
            return new FileChooser.ExtensionFilter(type.getFilename(),type.getExtension());
        }
        else
        {
            return new FileChooser.ExtensionFilter(type.getFilename(),type.getExtension());
        }

    }

    @FXML
    public void closeItemClicked()
    {
        if(isopened) {
            items.clear();
            tableview.getSelectionModel().clearSelection();
            tableview.refresh();
            tableview.setItems(items);
            isopened = false;
            fname = "";
            absolutePath = "";

            nameText.setText("");
            moneyText.setText("");
            serialText.setText("");
            errorLabel.setText("");

            addItemButton.setDisable(false);
            updateButton.setDisable(false);
            cancelButton.setDisable(false);
            deleteButton.setDisable(false);
        }

        else{
            Alert alert = new Alert(AlertType.ERROR);
            alert.setHeaderText("Error");
            alert.setContentText("Please Open A File To Close It");
            alert.show();

        }



    }
}

