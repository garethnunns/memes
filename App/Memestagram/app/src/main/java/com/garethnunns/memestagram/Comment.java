package com.garethnunns.memestagram;

/**
 * Created by gareth on 19/05/2017.
 */

public class Comment {
    public Long iduser;
    public String pp;
    public String username;
    public String ago;
    public String comment;

    public Comment(Long iduser, String pp,String username,String ago,String comment) {
        this.iduser = iduser;
        this.pp = pp;
        this.username = username;
        this.ago = ago;
        this.comment = comment;
    }
}
