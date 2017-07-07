package com.dublick.tutorial03.graph;

import org.smurn.jply.Element;
import org.smurn.jply.ElementReader;
import org.smurn.jply.ElementType;
import org.smurn.jply.PlyReader;
import org.smurn.jply.PlyReaderFile;

import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Created by 3dium on 07.07.2017.
 */

public class PlyLoader {

    private ArrayList<Float> vlist;
    private ArrayList<Short> ilist;

    public float[] getVertex(InputStream inputStream) throws Exception {

        try {
            readPly(inputStream);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        float[] vbo = new float[vlist.size()];
        for (int i = 0; i < vlist.size(); i++) {
            vbo[i] = vlist.get(i);
        }

        return vbo;
    }

    public short[] getIndexList() {
        if (ilist != null) {
            short[] ibo = new short[ilist.size()];
            for (int i = 0; i < ilist.size(); i++) {
                ibo[i] = ilist.get(i);
            }
            return ibo;
        }
        return null;
    }

    private void readPly(InputStream inputStream) throws IOException {

        PlyReader ply;
        ply = new PlyReaderFile(inputStream);

        ElementReader reader = ply.nextElementReader();
        while (reader != null) {

            ElementType type = reader.getElementType();
            if (type.getName().equals("vertex")) {
                readVertices(reader);
            }
            if (type.getName().equals("face")) {
                readFaces(reader);
            }
            reader.close();
            reader = ply.nextElementReader();
        }

        ply.close();
    }

    private void readVertices(ElementReader reader) throws IOException {
        Element element = reader.readElement();

        vlist = new ArrayList<>();
        DecimalFormat d = new DecimalFormat("#0.00");

        while (element != null) {
            vlist.add((float)element.getDouble("x"));
            vlist.add((float)element.getDouble("y"));
            vlist.add((float)element.getDouble("z"));

            vlist.add((float)element.getDouble("nx"));
            vlist.add((float)element.getDouble("ny"));
            vlist.add((float)element.getDouble("nz"));

            vlist.add((float)element.getInt("red")/255f);
            vlist.add((float)element.getDouble("green")/255f);
            vlist.add((float)element.getDouble("blue")/255f);
            vlist.add((float)element.getDouble("alpha")/255f);

            element = reader.readElement();
        }
    }

    private void readFaces(ElementReader reader) throws IOException {
        Element element = reader.readElement();
        ilist = new ArrayList<>();
        while (element != null) {
            int[] list = element.getIntList("vertex_indices");
            ilist.add((short) list[0]);
            ilist.add((short) list[1]);
            ilist.add((short) list[2]);

            element = reader.readElement();
        }
    }
}
