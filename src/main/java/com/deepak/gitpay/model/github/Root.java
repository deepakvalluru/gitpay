package com.deepak.gitpay.model.github;

import lombok.Data;

@Data
public class Root{
    private String token;
    private String job;
    private String ref;
    private String sha;
    private String repository;
    private String repository_owner;
    private String repositoryUrl;
    private String run_id;
    private String run_number;
    private String retention_days;
    private String actor;
    private String workflow;
    private String head_ref;
    private String base_ref;
    private String event_name;
    private Event event;
    private String server_url;
    private String api_url;
    private String graphql_url;
    private String workspace;
    private String action;
    private String event_path;
    private String action_repository;
    private String action_ref;
    private String path;
    private String env;
}
