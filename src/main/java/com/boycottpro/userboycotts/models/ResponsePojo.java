package com.boycottpro.userboycotts.models;

import java.util.List;

public class ResponsePojo {

    private String cause_id;
    private String cause_desc;
    private List<CompanySummary> companies;

    public ResponsePojo() {
    }

    public ResponsePojo(String cause_id, String cause_desc, List<CompanySummary> companies) {
        this.cause_id = cause_id;
        this.cause_desc = cause_desc;
        this.companies = companies;
    }

    public String getCause_id() {
        return cause_id;
    }

    public void setCause_id(String cause_id) {
        this.cause_id = cause_id;
    }

    public String getCause_desc() {
        return cause_desc;
    }

    public void setCause_desc(String cause_desc) {
        this.cause_desc = cause_desc;
    }

    public List<CompanySummary> getCompanies() {
        return companies;
    }

    public void setCompanies(List<CompanySummary> companies) {
        this.companies = companies;
    }

}

