package com.deepak.gitpay.model.github;

import lombok.Data;

@Data
public class Committer{
    public String email;
    public String name;
    public String username;
    public int watchers;
    public int watchers_count;
}
