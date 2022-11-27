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
        int ndoc = 0;
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
                ndoc++;
                if (ndoc % 100 == 0) {
                    System.out.println("Documento " + ndoc + " indexado en " + collection + ".");
                }
                doc.clear();
            }
        } catch (SolrServerException ex) {
            System.out.println("Error en Solrj.");
        } catch (IOException ex) {
            System.out.println("Error en Scanner.");
        }
        System.out.println("\nDocumentos de " + filename + " indexados en " + collection + ".\n");
    }

    public static Queue<String> parsearQRY(String filename) {

        Queue<String> docs = new LinkedList<>();
        Queue<String> words = new LinkedList<>();
        String line, field;
        String[] tokens;
        String total = "";
        Pattern pattern;
        Matcher matcher;
        int limit;
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
                    if (".I 112".equals(line)) {
                        //if (!scan.hasNextLine()) {
                        found = true;
                        total = total + ".X";
                    }
                }
            }
            pattern = Pattern.compile("(\\.W\n)(.*?)(\\.X)", Pattern.DOTALL);
            matcher = pattern.matcher(total);
            while (matcher.find()) {
                field = matcher.group(2).trim();
                docs.add(field);
            }
            int count = 0;
            while (!docs.isEmpty()) {
                count++;
                field = "";
                line = docs.remove();
                tokens = line.split("\\s+");

                /*if (tokens.length > 15) {
                    limit = 15;
                } else {
                    limit = tokens.length;
                }*/
                limit = tokens.length;
                if (tokens.length > 1) {
                    field = tokens[0].toLowerCase();
                    for (int i = 1; i < limit; i++) {
                        field = field + "||" + tokens[i].toLowerCase();
                    }
                    field = field.replaceAll("[\\+\\-\\&&\\!\\(\\)\\{\\}\\[\\]\\^\"\\~\\*\\?\\:\\/\\,\\.\\:\\;]", "");
                    //field = entidadesQRY(field);
                    words.add(field);
                    //System.out.println(count + ": " + field);
                }
            }
        } catch (IOException ex) {
            System.out.println("Error en Scanner.");
        }

        System.out.println(
                "\nConsultas de " + filename + " parseadas.\n");
        return words;
    }

    public static String entidadesQRY(String line) {

        Pattern pattern;
        Matcher matcher;
        Map<String, Integer> dic = new HashMap<String, Integer>();
        dic.put("information", 2);
        dic.put("retrieval", 4);
        dic.put("storage", 4);
        dic.put("consolidation", 4);
        dic.put("evaluation", 4);
        dic.put("automation", 4);
        dic.put("mechanization", 4);
        dic.put("translation", 4);
        dic.put("indexing", 4);
        dic.put("dissemination", 4);
        dic.put("training", 4);
        dic.put("cost", 4);
        dic.put("exchange", 4);
        dic.put("requests", 4);
        dic.put("language", 4);
        dic.put("articles", 2);
        dic.put("titles", 4);
        dic.put("agency", 2);
        dic.put("center", 2);
        dic.put("researchers", 2);
        dic.put("students", 2);
        dic.put("automated", 4);
        dic.put("medical", 4);
        dic.put("clinical", 4);
        dic.put("natural", 4);
        dic.put("social", 4);
        dic.put("chemical", 4);
        dic.put("alphabetical", 4);

        String word;
        int weight;
        Iterator<Map.Entry<String, Integer>> it = dic.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Integer> pair = it.next();
            word = pair.getKey();
            weight = pair.getValue();
            pattern = Pattern.compile(word);
            matcher = pattern.matcher(line);
            while (matcher.find()) {
                line = matcher.replaceAll(word + "^" + weight);
            }
        }
        return line;
    }

    public static SolrDocumentList[] consultar(String collection, Queue<String> words) {

        //Preparing the Solr client
        String urlString = "http://localhost:8983/solr/" + collection;
        SolrClient client = new HttpSolrClient.Builder(urlString).build();

        final SolrQuery query = new SolrQuery();
        QueryResponse rsp = new QueryResponse();
        SolrDocumentList docs = new SolrDocumentList();
        SolrDocumentList[] list = new SolrDocumentList[words.size() + 1];
        String line;
        int numquery;

        //System.out.println("\nResultados de las consultas en " + collection + ":");
        try {
            numquery = 0;
            while (!words.isEmpty()) {
                numquery++;
                line = words.remove();
                System.out.println("\n" + "Consulta: " + numquery);
                System.out.println(line);
                query.setQuery("text:" + line);
                //query.setQuery("Apple");
                //query.addFilterQuery("cat:electronics");
                query.setRows(40);
                query.setFields("ndoc", "score");
                rsp = client.query(query);
                docs = rsp.getResults();
                list[numquery] = docs;
                for (int i = 0; i < docs.size(); ++i) {
                    System.out.println(docs.get(i));
                }
            }
        } catch (SolrServerException ex) {
            System.out.println("Error en Solrj.");
        } catch (IOException ex) {
            System.out.println("Error en Scanner.");
        }
        System.out.println("\nConsultas en " + collection + " realizadas.\n");
        return list;
    }

    public static void crearTREC(SolrDocumentList[] list) {
        String nqry, ndoc, rank, score, line;
        int limit = 0;

        try {
            File trec = new File("trec_solr_file");
            if (trec.exists()) {
                trec.delete();
                System.out.println("\nArchivo " + trec.getName() + " sobreescrito.\n");
            } else {
                System.out.println("\nArchivo " + trec.getName() + " creado.\n");
            }
            trec.createNewFile();

            FileWriter writer = new FileWriter("trec_solr_file");
            for (int i = 1; i < list.length; i++) {
                nqry = String.format("%3d", i);
                for (int j = 0; j < list[i].size(); j++) {
                    ndoc = String.valueOf(list[i].get(j).getFieldValue("ndoc"));
                    ndoc = ndoc.replaceAll("[\\[\\]]", "");
                    ndoc = String.format("%4s", ndoc);
                    rank = String.format("%2d", j + 1);
                    score = String.valueOf(list[i].get(j).getFieldValue("score"));
                    if (score.length() < 8) {
                        limit = score.length();
                    } else {
                        limit = 8;
                    }
                    score = score.substring(0, limit);
                    score = String.format("%-8s", score);
                    line = nqry + " Q0 " + ndoc + " " + rank + " " + score + " DDP\n";
                    writer.write(line);
                }
            }
            writer.close();
        } catch (IOException e) {
            System.out.println("Error en File.");
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
