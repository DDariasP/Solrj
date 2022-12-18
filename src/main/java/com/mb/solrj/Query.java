/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mb.solrj;

/**
 *
 * @author Diego
 */
public class Query {

    private final String text;
    private final String person;
    private final String org;
    private final String location;
    private final String date;

    public Query(String text, String person, String org, String location, String date) {
        this.date = date;
        this.location = location;
        this.org = org;
        this.person = person;
        this.text = text;
    }

    public String getText() {
        return this.text;
    }

    public String getPerson() {
        return this.person;
    }

    public String getOrg() {
        return this.org;
    }

    public String getLocation() {
        return this.location;
    }

    public String getDate() {
        return this.date;
    }
}
