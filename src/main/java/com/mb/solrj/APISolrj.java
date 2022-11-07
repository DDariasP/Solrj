/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mb.solrj;

import java.io.*;
import java.util.*;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;

/**
 *
 * @author Diego
 */
public class APISolrj {

    /**
     *
     * @param filename
     * @param collection
     */
    public static void indexarCISI(String filename, String collection) {

        final SolrClient client = new HttpSolrClient.Builder("http://localhost:8983/solr").build();
        final SolrInputDocument doc = new SolrInputDocument();
        Queue<String> docs = new LinkedList<>();
        String line, field;
        String[] tokens;
        String total = "";
        Pattern pattern;
        Matcher matcher;
        try {
            Scanner scan = new Scanner(new File(filename));
            while (scan.hasNextLine()) {
                line = scan.nextLine();
                if (line.trim().length() > 0) {
                    total = total + line;
                    tokens = line.split("\\s+");
                    if (".X".equals(tokens[0])) {
                        docs.add(total);
                        total = "";
                    }
                }
            }
            while (!docs.isEmpty()) {
                line = docs.remove();

                pattern = Pattern.compile("(\\.I) ([0-9]*)");
                matcher = pattern.matcher(line);
                if (matcher.find()) {
                    field = matcher.group(2);
                    doc.addField("ndoc", field);
                }

                pattern = Pattern.compile("(\\.T)(.*?)(\\.A)");
                matcher = pattern.matcher(line);
                if (matcher.find()) {
                    field = matcher.group(2).trim();
                    doc.addField("title", field);
                }

                pattern = Pattern.compile("(\\.A)(.*?)(\\.W)");
                matcher = pattern.matcher(line);
                if (matcher.find()) {
                    field = matcher.group(2).trim();
                    doc.addField("author", field);
                }

                pattern = Pattern.compile("(\\.W)(.*?)(\\.X)");
                matcher = pattern.matcher(line);
                if (matcher.find()) {
                    field = matcher.group(2).trim();
                    doc.addField("text", field);
                }

                final UpdateResponse updateResponse = client.add(collection, doc);
                // Indexed documents must be committed
                client.commit(collection);
                //System.out.println("Documento " + ndoc + " indexado en " + collection + ".");
                doc.clear();
            }
        } catch (SolrServerException ex) {
            System.out.println("Error en Solrj.");
        } catch (IOException ex) {
            System.out.println("Error en Scanner.");
        }
        System.out.println("Documentos indexados en " + collection + ".");
    }

    public static void borrarTodo(String collection) {

        //Preparing the Solr client
        String urlString = "http://localhost:8983/solr/" + collection;
        SolrClient Solr = new HttpSolrClient.Builder(urlString).build();

        //Preparing the Solr document
        SolrInputDocument doc = new SolrInputDocument();

        try {
            //Deleting the documents from Solr
            Solr.deleteByQuery("*");
            Solr.commit();
            System.out.println("Documentos de " + collection + " borrados.");
        } catch (SolrServerException ex) {
            System.out.println("Error en Solrj.");
        } catch (IOException ex) {
            System.out.println("Error en Scanner.");
        }
    }
}
