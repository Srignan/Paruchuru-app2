package ucf.assignments;
/*
 *  UCF COP3330 Fall 2021 Application Assignment 2 Solution
 *  Copyright 2021 Srignan Paruchuru
 */
import java.io.Serializable;
import java.util.ArrayList;

public class AppData implements Serializable
{
    private ArrayList<Inventory> list;

    public AppData(ArrayList<Inventory> list)
    {
        this.list = list;
    }

    public ArrayList<Inventory> getList()
    {
        return list;
    }

}
