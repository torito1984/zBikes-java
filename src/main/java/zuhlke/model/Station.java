package zuhlke.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotBlank;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Station {

    public Station() { }

    public Station(String name, Location location, List<String> availableBikes) {
        this.name = name;
        this.location = location;
        this.availableBikes = availableBikes;
    }

    @JsonProperty("name")
    @NotBlank
    private String name;

    @JsonProperty("location")
    private Location location;

    @JsonProperty("availableBikes")
    private List<String> availableBikes;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public List<String> getAvailableBikes() {
        return availableBikes;
    }

    public void setAvailableBikes(List<String> availableBikes) {
        this.availableBikes = availableBikes;
    }

    @Override
    public String toString() {
        return "Station{" +
                "name='" + name + '\'' +
                ", location=" + location +
                ", availableBikes=" + availableBikes +
                '}';
    }
}
