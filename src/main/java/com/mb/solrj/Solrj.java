/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Project/Maven2/JavaApp/src/main/java/${packagePath}/${mainClassName}.java to edit this template
 */
package com.mb.solrj;

import static com.mb.solrj.APISolrj.borrarTodo;
import static com.mb.solrj.APISolrj.consultar;
import static com.mb.solrj.APISolrj.crearTREC;
import static com.mb.solrj.APISolrj.indexarCISI;
import static com.mb.solrj.APISolrj.indexarCISIGATE;
import java.util.Queue;
import static com.mb.solrj.APISolrj.parsearQRY;
import gate.util.GateException;
import org.apache.solr.common.SolrDocumentList;
import java.io.*;
import java.net.URISyntaxException;
import java.util.*;
import static com.mb.solrj.APISolrj.parsearAnnie;
import static com.mb.solrj.APISolrj.parsearQRYGATE;

/**
 *
 * @author Diego
 */
public class Solrj {

    public static void main(String[] args) throws GateException, IOException, URISyntaxException {
        String archivo1 = ".\\collection\\CISI.ALL";
        String archivo2 = ".\\collection\\CISI.QRY";
        String coleccion = "CISI";
        String salida = "QRY";
        SolrDocumentList[] lista;
        
        /*Queue<String> stopwords = new LinkedList<>();
        stopwords.add("of the from are is");
        stopwords.add("it no such that");
        consultar(coleccion, stopwords);*/
        
        //borrarTodo(coleccion);
        //indexarCISIGATE(archivo1,coleccion);
        Queue<Query> consultas = parsearQRYGATE(archivo2,salida);
        //lista = consultar(coleccion, consultas);
        //crearTREC(lista);

    }
}
