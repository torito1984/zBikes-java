package zuhlke.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotBlank;

public class Hire {

    public Hire() { }

    public Hire(String action, String username) {
        this.action = action;
        this.username = username;
    }

    @JsonProperty("action")
    @NotBlank
    private String action;

    @JsonProperty("username")
    @NotBlank
    private String username;


    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        return "Hire{" +
                "action='" + action + '\'' +
                ", username='" + username + '\'' +
                '}';
    }
}
