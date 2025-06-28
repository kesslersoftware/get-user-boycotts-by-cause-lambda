package com.boycottpro.userboycotts.models;

import java.util.List;

public class ResponsePojo {

    private boolean isFollowing;
    private String cause_id;
    private String cause_desc;
    private String followingSince;
    private List<CompanySummary> companies;

    public ResponsePojo() {
    }

    public ResponsePojo(boolean isFollowing, String cause_id, String cause_desc,
                        String followingSince, List<CompanySummary> companies) {
        this.isFollowing = isFollowing;
        this.cause_id = cause_id;
        this.cause_desc = cause_desc;
        this.followingSince = followingSince;
        this.companies = companies;
    }

    public boolean isFollowing() {
        return isFollowing;
    }

    public void setFollowing(boolean following) {
        isFollowing = following;
    }

    public String getFollowingSince() {
        return followingSince;
    }

    public void setFollowingSince(String followingSince) {
        this.followingSince = followingSince;
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

