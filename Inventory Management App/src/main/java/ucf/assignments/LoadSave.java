package ucf.assignments;
/*
 *  UCF COP3330 Fall 2021 Application Assignment 2 Solution
 *  Copyright 2021 Srignan Paruchuru
 */
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.w3c.dom.*;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class LoadSave
{
    public enum Type
    {
        TSV("TSV (Tab Separated Vale)","*.txt"),
        HTML("HTML","*.html"),
        JSON("JSON","*.json");

        private String filename;
        private String extension;

        Type(String filename, String extension)
        {
            this.filename = filename;
            this.extension = extension;
        }

        public String getFilename() {
            return filename;
        }

        public String getExtension() {
            return extension;
        }
    }

    public static ArrayList<Inventory> Open (File selectedFile) throws Exception
    {
        String name = selectedFile.getName();
        Type type;

        if (name.endsWith("txt"))
        {
            type = Type.TSV;
        }

        else if (name.endsWith("html"))
        {
            type = Type.HTML;
        }

        else if (name.endsWith("json"))
        {
            type = Type.JSON;
        }

        else
        {
            type = null;
        }

        try (FileReader reader = new FileReader(selectedFile))
        {
            switch (type)
            {
                case TSV: return openFromTSV(reader);
                case HTML: return openFromHTML(reader);
                case JSON: return openFromJSON(reader);
            }
        }
        return null;
    }


    private static ArrayList<Inventory> openFromTSV(Reader reader) throws IOException
    {
        ArrayList<Inventory> list = new ArrayList<>();
        BufferedReader buff = new BufferedReader(reader);
        String line;
        buff.readLine(); // ignore header
        while ( (line = buff.readLine()) != null)
        {
            String[] parts = line.split("\t");
            Inventory item = new Inventory(Double.parseDouble(parts[2]), parts[0], parts[1]);
            list.add(item);
        }

        return list;
    }

    private static ArrayList<Inventory> openFromJSON (Reader reader)
    {
        Gson gson = new Gson();
        return gson.fromJson(reader, TypeToken.getParameterized(List.class,Inventory.class).getType());
    }

    private static ArrayList<Inventory> openFromHTML(Reader reader) throws Exception
    {
        ArrayList<Inventory> list = new ArrayList<>();
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(reader));

        Node table = document.getElementsByTagName("table").item(0);

        if (table.hasChildNodes())
        {
            NodeList rows = table.getChildNodes();
            int rCount = 0;
            for (int i = 0; i < rows.getLength(); i++)
            {
                Node row = rows.item(i);
                if (row.getNodeName().equals("tr"))
                {
                    rCount++;

                    if (rCount == 1)
                        continue;

                    NodeList cols = row.getChildNodes();

                    int cCount = 0;

                    Inventory item = new Inventory();

                    for (int j = 0; j < cols.getLength(); j++)
                    {
                        Node col = cols.item(j);
                        if (col.getNodeName().equals("td"))
                        {
                            switch (cCount) {
                                case 0 -> {
                                    item.setValue(Double.parseDouble(col.getTextContent()));
                                }
                                case 1 -> {
                                    item.setSerial_number(col.getTextContent());
                                }
                                case 2 -> {
                                    item.setName(col.getTextContent());
                                }
                            }
                            cCount++;
                        }
                    }
                    list.add(item);
                }
            }
        }
        return list;
    }



    public static void Save_As(File selectedFile, Type type, ArrayList<Inventory> list) throws Exception
    {
        try (FileWriter writer = new FileWriter(selectedFile))
        {
            switch (type) {
                case TSV -> Save_As_TSV(writer, list);
                case HTML -> Save_As_HTML(writer, list);
                case JSON -> Save_As_JSON(writer, list);
            }

        }
    }

    public static void Save_As_TSV(Writer writer, ArrayList<Inventory> list)
    {
        PrintWriter pw = new PrintWriter(writer);
        pw.println("Serial Number\tName\tValue");

        for (Inventory inventory : list) {
            pw.println(inventory.getSerial_number() + "\t" + inventory.getName() + "\t" + String.format("%.2f", inventory.getValue()));

        }

    }

    public static void Save_As_JSON(Writer writer, ArrayList<Inventory> list)
    {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        gson.toJson(list,writer);
    }

    public static void Save_As_HTML(Writer writer, ArrayList<Inventory> list) throws Exception
    {
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element html = document.createElement("html");
        Element body = document.createElement("body");
        Element table = document.createElement("table");

        Element tr;
        Element value;
        Element serialNo;
        Element name;


        tr = document.createElement("tr");

        value = document.createElement("td");
        serialNo = document.createElement("td");
        name = document.createElement("td");


        value.setTextContent("Value");
        serialNo.setTextContent("Serial Number");
        name.setTextContent("Name");


        tr.appendChild(value);
        tr.appendChild(serialNo);
        tr.appendChild(name);


        table.appendChild(tr);

        for (Inventory inventory : list) {
            tr = document.createElement("tr");

            value = document.createElement("td");
            serialNo = document.createElement("td");
            name = document.createElement("td");


            value.setTextContent(String.format("%.2f", inventory.getValue()));
            serialNo.setTextContent(inventory.getSerial_number());
            name.setTextContent(inventory.getName());

            tr.appendChild(value);
            tr.appendChild(serialNo);
            tr.appendChild(name);


            table.appendChild(tr);
        }


        body.appendChild(table);
        html.appendChild(body);
        document.appendChild(html);

        DOMImplementationLS domImpl = ((DOMImplementationLS) DOMImplementationRegistry.newInstance().getDOMImplementation("LS"));
        LSSerializer serializer = domImpl.createLSSerializer();
        DOMConfiguration conf = serializer.getDomConfig();
        conf.setParameter("format-pretty-print", true);
        LSOutput output = domImpl.createLSOutput();
        output.setCharacterStream(writer);
        serializer.write(document,output);
    }


}
