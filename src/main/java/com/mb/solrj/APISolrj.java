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
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrDocumentList;
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
        System.out.println("\nDocumentos indexados en " + collection + ".\n");
    }

    public static Queue<String> indexarQRY(String filename) {

        Queue<String> docs = new LinkedList<>();
        Queue<String> words = new LinkedList<>();
        String line, field;
        String[] tokens;
        String total = "";
        Pattern pattern;
        Matcher matcher;
        try {
            Scanner scan = new Scanner(new File(filename));
            boolean found = false;
            while (!found) {
                line = scan.nextLine();
                if (line.trim().length() > 0) {
                    tokens = line.split("\\s+");
                    if (!".I".equals(tokens[0])) {
                        total = total + "\n" + line;
                    } else {
                        total = total + ".X";
                    }
                    if (".I 58".equals(line)) {
                        found = true;
                    }
                }
            }
            pattern = Pattern.compile("(\\.W\n)(.*?)(\\.X)", Pattern.DOTALL);
            matcher = pattern.matcher(total);
            while (matcher.find()) {
                field = matcher.group(2).trim();
                docs.add(field);
            }
            while (!docs.isEmpty()) {
                field = "";
                line = docs.remove();
                tokens = line.split("\\s+");
                if (tokens.length > 5) {
                    for (int i = 0; i < 5; i++) {
                        field = field + " " + tokens[i];
                    }
                    words.add(field);
                }
            }
        } catch (IOException ex) {
            System.out.println("Error en Scanner.");
        }
        System.out.println("\nConsultas de " + filename + " indexadas.\n");
        return words;
    }

    public static void consultar(String collection, Queue<String> words) {

        //Preparing the Solr client
        String urlString = "http://localhost:8983/solr/" + collection;
        SolrClient client = new HttpSolrClient.Builder(urlString).build();

        final SolrQuery query = new SolrQuery();
        QueryResponse rsp = new QueryResponse();
        SolrDocumentList docs = new SolrDocumentList();
        String line;

        System.out.println("\nResultados de las consultas en " + collection + ":");

        try {
            while (!words.isEmpty()) {
                line = words.remove();
                System.out.println("\n" + line);
                query.setQuery("text:" + line);
                //query.setQuery("Apple");
                //query.addFilterQuery("cat:electronics");
                query.setFields("ndoc","author","score");
                rsp = client.query(query);
                docs = rsp.getResults();
                for (int i = 0; i < docs.size(); ++i) {
                    System.out.println(docs.get(i));
                }
            }
        } catch (SolrServerException ex) {
            System.out.println("Error en Solrj.");
        } catch (IOException ex) {
            System.out.println("Error en Scanner.");
        }
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
            System.out.println("\nDocumentos de " + collection + " borrados.\n");
        } catch (SolrServerException ex) {
            System.out.println("Error en Solrj.");
        } catch (IOException ex) {
            System.out.println("Error en Scanner.");
        }
    }
}
