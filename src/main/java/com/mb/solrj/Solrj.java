/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Project/Maven2/JavaApp/src/main/java/${packagePath}/${mainClassName}.java to edit this template
 */
package com.mb.solrj;

import static com.mb.solrj.APISolrj.borrarTodo;
import static com.mb.solrj.APISolrj.consultar;
import static com.mb.solrj.APISolrj.crearTREC;
import static com.mb.solrj.APISolrj.indexarCISI;
import java.util.Queue;
import static com.mb.solrj.APISolrj.parsearQRY;
import org.apache.solr.common.SolrDocumentList;

/**
 *
 * @author Diego
 */
public class Solrj {

    public static void main(String[] args) {
        String archivo1 = ".\\collection\\CISI.ALL";
        String archivo2 = ".\\collection\\CISI.QRY";
        String coleccion = "CISI";
        SolrDocumentList[] lista;

        //borrarTodo(coleccion);
        //indexarCISI(archivo1,coleccion);
        Queue<String> consultas = parsearQRY(archivo2);
        lista = consultar(coleccion, consultas);
        crearTREC(lista);

    }
}
