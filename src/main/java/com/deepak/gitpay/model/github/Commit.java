package com.deepak.gitpay.model.github;

import lombok.Data;
import lombok.Getter;

import java.util.Date;

@Data
public class Commit{
    private  Author author;
    private  Committer committer;
    private  boolean distinct;
    private  String id;
    private  String message;
    private  Date timestamp;
    private  String tree_id;
    private  String url;
}
