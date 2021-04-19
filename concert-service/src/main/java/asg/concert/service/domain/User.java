package asg.concert.service.domain;

import javax.persistence.*;

@Entity
@Table(name="users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String Username;
    private String Password;
    @Version
    private long version;

    public void setUsername(String username) {
        Username = username;
    }

    public String getUsername() {
        return Username;
    }

    public void setPassword(String password) {
        Password = password;
    }

    public String getPassword() {
        return Password;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public long getVersion() {
        return version;
    }
}
