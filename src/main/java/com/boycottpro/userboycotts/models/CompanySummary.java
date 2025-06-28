package com.boycottpro.userboycotts.models;

public class CompanySummary {
    private String company_id;
    private String company_name;

    public CompanySummary() {
    }

    public CompanySummary(String company_id, String company_name) {
        this.company_id = company_id;
        this.company_name = company_name;
    }

    public String getCompany_id() {
        return company_id;
    }

    public void setCompany_id(String company_id) {
        this.company_id = company_id;
    }

    public String getCompany_name() {
        return company_name;
    }

    public void setCompany_name(String company_name) {
        this.company_name = company_name;
    }
}
