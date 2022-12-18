/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mb.solrj;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Corpus;
import gate.Document;
import gate.Factory;
import gate.FeatureMap;
import gate.Gate;
import gate.GateConstants;
import gate.corpora.RepositioningInfo;
import gate.util.GateException;
import gate.util.Out;
import java.io.*;
import java.util.*;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
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

    /**
     *
     * @param filename
     * @param collection
     */
    public static void indexarCISIGATE(String filename, String collection) {

        String line;
        String[] tokens;
        boolean next = true;

        try {
            File corpus = new File("corpus.txt");
            if (corpus.exists()) {
                corpus.delete();
                System.out.println("\nArchivo " + corpus.getName() + " sobreescrito.\n");
            } else {
                System.out.println("\nArchivo " + corpus.getName() + " creado.\n");
            }
            corpus.createNewFile();

            Scanner scan = new Scanner(new File(filename));
            FileWriter writer = new FileWriter("corpus.txt");

            while (scan.hasNextLine()) {
                line = scan.nextLine();
                if (line.trim().length() > 0) {
                    tokens = line.split("\\s+");
                    if (".X".equals(tokens[0])) {
                        writer.write(line + "\n");
                        next = false;
                    }
                    if (".I".equals(tokens[0])) {
                        next = true;
                    }
                    if (next) {
                        line = line.replaceAll("[\\+\\-\\&&\\!\\(\\)\\{\\}\\[\\]\\^\"\\~\\*\\?\\:\\/\\'\\,\\:\\;]", "");
                        writer.write(line + "\n");
                    }
                }
            }
            writer.close();

            filename = parsearAnnie(corpus.getName(), collection);
            //filename = collection + ".xml";

            final SolrClient client = new HttpSolrClient.Builder("http://localhost:8983/solr").build();
            final SolrInputDocument doc = new SolrInputDocument();
            Queue<String> docs = new LinkedList<>();
            String field;
            String total = "";
            Pattern pattern;
            Matcher matcher;
            int ndoc = 0;

            scan = new Scanner(new File(filename));
            while (scan.hasNextLine()) {
                line = scan.nextLine();
                if (line.trim().length() > 0) {
                    total = total + " " + line;
                    tokens = line.split("\\s+");
                    if (".X".equals(tokens[0])) {
                        docs.add(total);
                        total = "";
                    }
                }
            }
            while (!docs.isEmpty()) {
                line = docs.remove();

                //Campos ANNIE
                pattern = Pattern.compile("(<Person>)(.*?)(</Person>)");
                matcher = pattern.matcher(line);
                while (matcher.find()) {
                    field = matcher.group(2);
                    doc.addField("person", field);
                }
                line = line.replaceAll("<Person>", "");
                line = line.replaceAll("</Person>", "");

                pattern = Pattern.compile("(<Organization>)(.*?)(</Organization>)");
                matcher = pattern.matcher(line);
                while (matcher.find()) {
                    field = matcher.group(2);
                    doc.addField("org", field);
                }
                line = line.replaceAll("<Organization>", "");
                line = line.replaceAll("</Organization>", "");

                pattern = Pattern.compile("(<Location>)(.*?)(</Location>)");
                matcher = pattern.matcher(line);
                while (matcher.find()) {
                    field = matcher.group(2);
                    doc.addField("location", field);
                }
                line = line.replaceAll("<Location>", "");
                line = line.replaceAll("</Location>", "");

                pattern = Pattern.compile("(<Date>)(.*?)(</Date>)");
                matcher = pattern.matcher(line);
                while (matcher.find()) {
                    field = matcher.group(2);
                    doc.addField("date", field);
                }
                line = line.replaceAll("<Date>", "");
                line = line.replaceAll("</Date>", "");

                //Campos básicos
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
        } catch (FileNotFoundException ex) {
        } catch (SolrServerException ex) {
        } catch (IOException e) {
        }
    }

    /**
     *
     * @param filename
     * @param output
     * @return
     */
    public static String parsearAnnie(String filename, String output) {

        try {

            // initialise the GATE library
            Out.prln("Initialising GATE...");
            Gate.init();
            Out.prln("...GATE initialised");

            // initialise ANNIE (this may take several minutes)
            StandAloneAnnie annie = new StandAloneAnnie();
            annie.initAnnie();

            // create a GATE qry and add a document for each command-line
            // argument
            Corpus corpus = Factory.newCorpus("StandAloneAnnie corpus");

            URL u = new URL("file:" + filename);
            FeatureMap params = Factory.newFeatureMap();
            params.put("sourceUrl", u);
            params.put("preserveOriginalContent", new Boolean(true));
            params.put("collectRepositioningInfo", new Boolean(true));
            Out.prln("Creating doc for " + u);
            Document doc = (Document) Factory.createResource("gate.corpora.DocumentImpl", params);
            corpus.add(doc);

            // tell the pipeline about the qry and run it
            annie.setCorpus(corpus);
            annie.execute();

            // for each document, get an XML document with the
            // person and location names added
            Iterator iter = corpus.iterator();
            String startTagPart_1 = "<span GateID=\"";
            String startTagPart_2 = "\" title=\"";
            String startTagPart_3 = "\" style=\"background:Red;\">";
            String endTag = "</span>";

            while (iter.hasNext()) {
                doc = (Document) iter.next();
                AnnotationSet defaultAnnotSet = doc.getAnnotations();
                Set annotTypesRequired = new HashSet();
                annotTypesRequired.add("Person");
                annotTypesRequired.add("Organization");
                annotTypesRequired.add("Location");
                annotTypesRequired.add("Date");
                Set<Annotation> anno
                        = new HashSet<Annotation>(defaultAnnotSet.get(annotTypesRequired));

                FeatureMap features = doc.getFeatures();
                String originalContent = (String) features.get(GateConstants.ORIGINAL_DOCUMENT_CONTENT_FEATURE_NAME);
                RepositioningInfo info = (RepositioningInfo) features.get(GateConstants.DOCUMENT_REPOSITIONING_INFO_FEATURE_NAME);

                File file = new File(output + ".html");
                Out.prln("File name: '" + file.getAbsolutePath() + "'");
                if (originalContent != null && info != null) {
                    Out.prln("OrigContent and reposInfo existing. Generate file...");

                    Iterator it = anno.iterator();
                    Annotation currAnnot;
                    StandAloneAnnie.SortedAnnotationList sortedAnnotations = new StandAloneAnnie.SortedAnnotationList();

                    while (it.hasNext()) {
                        currAnnot = (Annotation) it.next();
                        sortedAnnotations.addSortedExclusive(currAnnot);
                    } // while

                    StringBuffer editableContent = new StringBuffer(originalContent);
                    long insertPositionEnd;
                    long insertPositionStart;
                    // insert anotation tags backward
                    Out.prln("Unsorted annotations count: " + anno.size());
                    Out.prln("Sorted annotations count: " + sortedAnnotations.size());
                    for (int i = sortedAnnotations.size() - 1; i >= 0; --i) {
                        currAnnot = (Annotation) sortedAnnotations.get(i);
                        insertPositionStart
                                = currAnnot.getStartNode().getOffset().longValue();
                        insertPositionStart = info.getOriginalPos(insertPositionStart);
                        insertPositionEnd = currAnnot.getEndNode().getOffset().longValue();
                        insertPositionEnd = info.getOriginalPos(insertPositionEnd, true);
                        if (insertPositionEnd != -1 && insertPositionStart != -1) {
                            editableContent.insert((int) insertPositionEnd, endTag);
                            editableContent.insert((int) insertPositionStart, startTagPart_3);
                            editableContent.insert((int) insertPositionStart,
                                    currAnnot.getType());
                            editableContent.insert((int) insertPositionStart, startTagPart_2);
                            editableContent.insert((int) insertPositionStart,
                                    currAnnot.getId().toString());
                            editableContent.insert((int) insertPositionStart, startTagPart_1);
                        } // if
                    } // for

                    FileWriter writer = new FileWriter(file);
                    writer.write(editableContent.toString());
                    writer.close();
                } // if - should generate
                else if (originalContent != null) {
                    Out.prln("OrigContent existing. Generate file...");

                    Iterator it = anno.iterator();
                    Annotation currAnnot;
                    StandAloneAnnie.SortedAnnotationList sortedAnnotations = new StandAloneAnnie.SortedAnnotationList();

                    while (it.hasNext()) {
                        currAnnot = (Annotation) it.next();
                        sortedAnnotations.addSortedExclusive(currAnnot);
                    } // while

                    StringBuffer editableContent = new StringBuffer(originalContent);
                    long insertPositionEnd;
                    long insertPositionStart;
                    // insert anotation tags backward
                    Out.prln("Unsorted annotations count: " + anno.size());
                    Out.prln("Sorted annotations count: " + sortedAnnotations.size());
                    for (int i = sortedAnnotations.size() - 1; i >= 0; --i) {
                        currAnnot = (Annotation) sortedAnnotations.get(i);
                        insertPositionStart
                                = currAnnot.getStartNode().getOffset().longValue();
                        insertPositionEnd = currAnnot.getEndNode().getOffset().longValue();
                        if (insertPositionEnd != -1 && insertPositionStart != -1) {
                            editableContent.insert((int) insertPositionEnd, endTag);
                            editableContent.insert((int) insertPositionStart, startTagPart_3);
                            editableContent.insert((int) insertPositionStart,
                                    currAnnot.getType());
                            editableContent.insert((int) insertPositionStart, startTagPart_2);
                            editableContent.insert((int) insertPositionStart,
                                    currAnnot.getId().toString());
                            editableContent.insert((int) insertPositionStart, startTagPart_1);
                        } // if
                    } // for

                    FileWriter writer = new FileWriter(file);
                    writer.write(editableContent.toString());
                    writer.close();
                } else {
                    Out.prln("Content : " + originalContent);
                    Out.prln("Repositioning: " + info);
                }

                String xmlDocument = doc.toXml(anno, false);
                String fileName = new String(output + ".xml");
                FileWriter writer = new FileWriter(fileName);
                writer.write(xmlDocument);
                writer.close();

            }

        } catch (IOException e) {
        } catch (GateException ex) {
        }

        filename = output + ".xml";
        return filename;

    }

    /**
     *
     * @param filename
     * @return
     */
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

                if (tokens.length > 110) {
                    limit = 110; //numero optimo de palabras por consulta
                } else {
                    limit = tokens.length;
                }
                //limit = tokens.length;
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

        System.out.println("\nConsultas de " + filename + " parseadas.\n");
        return words;
    }

    /**
     *
     * @param filename
     * @param output
     * @return
     */
    public static Queue<Query> parsearQRYGATE(String filename, String output) {

        Queue<String> text = parsearQRY(filename);
        Queue<Query> list = new LinkedList<>();

        try {
            File qry = new File("qry.txt");
            if (qry.exists()) {
                qry.delete();
                System.out.println("\nArchivo " + qry.getName() + " sobreescrito.\n");
            } else {
                System.out.println("\nArchivo " + qry.getName() + " creado.\n");
            }
            qry.createNewFile();

            Scanner scan = new Scanner(new File(filename));
            FileWriter writer = new FileWriter("qry.txt");

            String line;
            boolean found = false;
            while (!found) {
                line = scan.nextLine();
                if (".I 112".equals(line)) {
                    found = true;
                    writer.write(line + "\n");
                }
                if (line.trim().length() > 0 && !found) {
                    line = line.replaceAll("[\\+\\-\\&&\\!\\(\\)\\{\\}\\[\\]\\^\"\\~\\*\\?\\:\\/\\'\\,\\:\\;]", "");
                    writer.write(line + "\n");
                }
            }
            writer.close();

            filename = parsearAnnie(qry.getName(), output);
            //filename = output + ".xml";

            Queue<String> docs = new LinkedList<>();
            String field;
            String total = "";
            Pattern pattern;
            Matcher matcher;
            String[] tokens;
            int count = 0;

            scan = new Scanner(new File(filename));
            while (scan.hasNextLine()) {
                line = scan.nextLine();
                if (line.trim().length() > 0) {
                    total = total + " " + line;
                    tokens = line.split("\\s+");
                    if (".I".equals(tokens[0]) && count != 0) {
                        docs.add(total);
                        System.out.println(total);
                        total = line;
                        count = 0;
                    } else {
                        count++;
                    }
                }
            }

            String person, org, location, date;
            while (!docs.isEmpty()) {
                line = docs.remove();
                //Campos ANNIE
                person = "";
                org = "";
                location = "";
                date = "";

                pattern = Pattern.compile("(<Person>)(.*?)(</Person>)");
                matcher = pattern.matcher(line);
                while (matcher.find()) {
                    field = matcher.group(2);
                    person = person + field + "||";
                }
                if (person.length() > 0) {
                    person = person.substring(0, person.length() - 2);
                }

                pattern = Pattern.compile("(<Organization>)(.*?)(</Organization>)");
                matcher = pattern.matcher(line);
                while (matcher.find()) {
                    field = matcher.group(2);
                    org = org + field + "||";
                }
                if (org.length() > 0) {
                    org = org.substring(0, org.length() - 2);
                }

                pattern = Pattern.compile("(<Location>)(.*?)(</Location>)");
                matcher = pattern.matcher(line);
                while (matcher.find()) {
                    field = matcher.group(2);
                    location = location + field + "||";
                }
                if (location.length() > 0) {
                    location = location.substring(0, location.length() - 2);
                }

                pattern = Pattern.compile("(<Date>)(.*?)(</Date>)");
                matcher = pattern.matcher(line);
                while (matcher.find()) {
                    field = matcher.group(2);
                    date = date + field + "||";
                }
                if (date.length() > 0) {
                    date = date.substring(0, date.length() - 2);
                }

                Query q = new Query(text.remove(), person, org, location, date);
                //System.out.println(q.getDate() + "\n" + q.getLocation() + "\n" + q.getOrg() + "\n" + q.getPerson() + "\n" + q.getText());
                list.add(q);
            }

        } catch (FileNotFoundException ex) {

        } catch (IOException e) {
        }

        return list;
    }

    /**
     *
     * @param line
     * @return
     */
    public static String entidadesQRY(String line) {

        Pattern pattern;
        Matcher matcher;
        Map<String, Double> dic = new HashMap<String, Double>();
        dic.put("information", 0.1);
        dic.put("library", 0.1);
        dic.put("libraries", 0.1);
        dic.put("system", 0.1);
        dic.put("develop", 0.1);
        dic.put("development", 0.1);
        dic.put("study", 0.1);
        dic.put("studies", 0.1);
        dic.put("research", 0.1);
        dic.put("researcher", 0.1);
        dic.put("researchers", 0.1);
        dic.put("data", 0.1);
        dic.put("some", 0.1);
        dic.put("book", 0.1);
        dic.put("result", 0.1);
        dic.put("other", 0.1);
        dic.put("problem", 0.1);
        dic.put("problems", 0.1);
        dic.put("center", 0.1);
        dic.put("paper", 0.1);
        dic.put("papers", 0.1);
        dic.put("base", 0.1);
        dic.put("based", 0.1);
        dic.put("describe", 0.1);
        dic.put("retrieval", 0.1);
        dic.put("work", 0.1);
        dic.put("index", 0.1);
        dic.put("indexing", 0.1);

        String word;
        double weight;
        Iterator<Map.Entry<String, Double>> it = dic.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Double> pair = it.next();
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

    /**
     *
     * @param collection
     * @param words
     * @return
     */
    public static SolrDocumentList[] consultar(String collection, Queue<String> words) {

        //Preparing the Solr client
        String urlString = "http://localhost:8983/solr/" + collection;
        SolrClient client = new HttpSolrClient.Builder(urlString).build();

        final SolrQuery query = new SolrQuery();
        QueryResponse rsp = new QueryResponse();
        SolrDocumentList docs = new SolrDocumentList();
        SolrDocumentList[] list = new SolrDocumentList[words.size() + 1];
        String line;
        int numquery, numempty;
        numquery = 0;
        numempty = 0;
        //System.out.println("\nResultados de las consultas en " + collection + ":");
        try {
            while (!words.isEmpty()) {
                numquery++;
                line = words.remove();
                System.out.println("\n" + "Consulta: " + numquery);
                System.out.println(line);
                query.setQuery("text:" + line);
                //query.setQuery("Apple");
                //query.addFilterQuery("cat:electronics");
                query.setRows(100); //numero optimo de docs por consulta
                query.setFields("ndoc", "score");
                rsp = client.query(query);
                docs = rsp.getResults();
                list[numquery] = docs;
                if (list[numquery].isEmpty()) {
                    numempty++;
                }
                for (int i = 0; i < docs.size(); ++i) {
                    System.out.println(docs.get(i));
                }
            }
        } catch (SolrServerException ex) {
            System.out.println("Error en Solrj.");
        } catch (IOException ex) {
            System.out.println("Error en Scanner.");
        }
        System.out.println("\n" + numquery + " consultas en " + collection + " realizadas.");
        System.out.println("\n" + numempty + " consultas sin resultados.");
        return list;
    }

    public static SolrDocumentList[] consultarGATE(String collection, Queue<Query> qlist) {

        //Preparing the Solr client
        String urlString = "http://localhost:8983/solr/" + collection;
        SolrClient client = new HttpSolrClient.Builder(urlString).build();

        final SolrQuery query = new SolrQuery();
        QueryResponse rsp = new QueryResponse();
        SolrDocumentList docs = new SolrDocumentList();
        SolrDocumentList[] list = new SolrDocumentList[qlist.size() + 1];
        String line;
        Query q;
        int numquery, numempty;
        numquery = 0;
        numempty = 0;
        String total;
        //System.out.println("\nResultados de las consultas en " + collection + ":");
        try {
            while (!qlist.isEmpty()) {
                numquery++;
                q = qlist.remove();
                System.out.println("\nConsulta: " + numquery);
                total = "text:" + q.getText();
                if (!q.getDate().equals("")) {
                    total = total + " OR date:" + q.getDate();
                }
                if (!q.getLocation().equals("")) {
                    total = total + " OR location:" + q.getLocation();
                }
                if (!q.getOrg().equals("")) {
                    total = total + " OR org:" + q.getOrg();
                }
                if (!q.getPerson().equals("")) {
                    total = total + " OR person:" + q.getPerson();
                }
                System.out.println(total);
                query.setQuery(total);
                query.setRows(100); //numero optimo de docs por consulta
                query.setFields("ndoc", "score");
                rsp = client.query(query);
                docs = rsp.getResults();
                list[numquery] = docs;
                if (list[numquery].isEmpty()) {
                    numempty++;
                }
                for (int i = 0; i < docs.size(); ++i) {
                    System.out.println(docs.get(i));
                }
            }
        } catch (SolrServerException ex) {
            System.out.println("Error en Solrj.");
        } catch (IOException ex) {
            System.out.println("Error en Scanner.");
        }
        System.out.println("\n" + numquery + " consultas en " + collection + " realizadas.");
        System.out.println("\n" + numempty + " consultas sin resultados.");
        return list;
    }

    /**
     *
     * @param list
     */
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
                    score = String.valueOf(list[i].get(j).getFieldValue("score"));
                    if (Double.valueOf(score) > 1.6) { //score minimo aceptado
                        if (score.length() < 8) {
                            limit = score.length();
                        } else {
                            limit = 8;
                        }
                        score = score.substring(0, limit);
                        score = String.format("%-8s", score);
                        ndoc = String.valueOf(list[i].get(j).getFieldValue("ndoc"));
                        ndoc = ndoc.replaceAll("[\\[\\]]", "");
                        ndoc = String.format("%4s", ndoc);
                        rank = String.format("%2d", j + 1);
                        line = nqry + " Q0 " + ndoc + " " + rank + " " + score + " DDP\n";
                        writer.write(line);
                    }
                }
            }
            writer.close();
        } catch (IOException e) {
            System.out.println("Error en File.");
        }
    }

    /**
     *
     * @param collection
     */
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
