package com.deepak.gitpay.model.github;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;

@Data
public class Repository{
    private  String archive_url;
    private  boolean archived;
    private  String assignees_url;
    private  String blobs_url;
    private  String branches_url;
    private  String clone_url;
    private  String collaborators_url;
    private  String comments_url;
    private  String commits_url;
    private  String compare_url;
    private  String contents_url;
    private  String contributors_url;
    private  int created_at;
    private  String default_branch;
    private  String deployments_url;
    private  Object description;
    private  boolean disabled;
    private  String downloads_url;
    private  String events_url;
    private  boolean fork;
    private  int forks;
    private  int forks_count;
    private  String forks_url;
    private  String full_name;
    private  String git_commits_url;
    private  String git_refs_url;
    private  String git_tags_url;
    private  String git_url;
    private  boolean has_downloads;
    private  boolean has_issues;
    private  boolean has_pages;
    private  boolean has_projects;
    private  boolean has_wiki;
    private  Object homepage;
    private  String hooks_url;
    private  String html_url;
    private  int id;
    private  String issue_comment_url;
    private  String issue_events_url;
    private  String issues_url;
    private  String keys_url;
    private  String labels_url;
    private  String language;
    private  String languages_url;
    private  Object license;
    private  String master_branch;
    private  String merges_url;
    private  String milestones_url;
    private  Object mirror_url;
    private  String name;
    private  String node_id;
    private  String notifications_url;
    private  int open_issues;
    private  int open_issues_count;
    private  User owner;
    @JsonProperty("private")
    private  boolean isPrivate;
    private  String pulls_url;
    private  int pushed_at;
    private  String releases_url;
    private  int size;
    private  String ssh_url;
    private  int stargazers;
    private  int stargazers_count;
    private  String stargazers_url;
    private  String statuses_url;
    private  String subscribers_url;
    private  String subscription_url;
    private  String svn_url;
    private  String tags_url;
    private  String teams_url;
    private  String trees_url;
    private  Date updated_at;
    private  String url;
    private  int watchers;
    private  int watchers_count;
}