package com.deepak.gitpay.model.github;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class Event{
    private String after;
    private Object base_ref;
    private String before;
    private List<Commit> commits;
    private String compare;
    private boolean created;
    private boolean deleted;
    private boolean forced;
    @JsonProperty("head_commit")
    private Commit headCommit;
    private User pusher;
    private String ref;
    private Repository  repository;
    private User sender;
}
