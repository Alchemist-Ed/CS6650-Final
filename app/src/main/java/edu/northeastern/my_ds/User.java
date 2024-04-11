package edu.northeastern.my_ds;

public class User {
    private String id; // user's firebase uid
    private String username; // username is unique
    private String email;
    private String text;

    public User(String id, String username, String email){
        this.id = id;
        this.username = username;
        this.email = email;
    }

    public String getId(){
        return id;
    }
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
