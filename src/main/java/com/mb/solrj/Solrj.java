/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Project/Maven2/JavaApp/src/main/java/${packagePath}/${mainClassName}.java to edit this template
 */
package com.mb.solrj;

import static com.mb.solrj.APISolrj.borrarTodo;
import static com.mb.solrj.APISolrj.consultar;
import static com.mb.solrj.APISolrj.indexarCISI;
import java.util.Queue;
import static com.mb.solrj.APISolrj.parsearQRY;

/**
 *
 * @author Diego
 */
public class Solrj {

    public static void main(String[] args) {
        String archivo = ".\\collection\\CISI.QRY";
        String coleccion = "CISI";

        Queue<String> consultas = parsearQRY(archivo);
        consultar(coleccion,consultas);

    }
}
