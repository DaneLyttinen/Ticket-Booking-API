package asg.concert.service.domain;

import javax.persistence.*;

@Entity
@Table(name="users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String username;
    private String password;
    @Version  // honestly, don't know what the version field is for. Maybe it's something to do with the database, hence using this?
    private long version;
    private String token;
    
    public User() {} // JPA requirement for Entity

    // this shouldn't be used directly by our code because the database makes the id
    public User(long id, String username, String password, long version) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.version = version;
        this.token = null; // yes, this is done without me doing this, but clarity
    }
 
     public long getId() { 
         return this.id;
     }
 
     public void setId(long id) { // doesn't make sense than any of these classes would have setters for their id's (since the database decides them?)
         this.id = id;
     }
 
     public String getUsername() {
         return this.username;
     }
 
     public void setUsername(String username) {
         this.username = username;
     }
 
     public String getPassword() {
         return this.password;
     }
 
     public void setPassword(String password) {
         this.password = password;
     }
 
     public long getVersion() {
         return this.version;
     }
 
     public void setVersion(long version) {
         this.version = version;
     }
 
     @Override
     public String toString() {
         return String.format("Id: '%d', username: '%s', password: '%s', version: '%s'", this.getId(), this.getUsername(), this.getPassword(), this.getVersion());
     }

     public String getToken() {
         return this.token;
     }

     public void setToken(String token) {
         this.token = token;
     }
 
     @Override
     public boolean equals(Object aObject) {
         User otherUser = (User) aObject; // TODO: if this throws an error, return false
         return this.getId() == otherUser.getId(); // because id is made by JPA, and we can guarantee it's unique
     }


}
