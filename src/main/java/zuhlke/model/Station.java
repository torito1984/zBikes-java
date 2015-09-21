package zuhlke.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotBlank;

import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Station {

    public Station() { }

    public Station(String name, Location location, Set<String> availableBikes, Integer availableBikeCount, String selfUrl, String hireUrl) {
        this.name = name;
        this.location = location;
        this.availableBikes = availableBikes;
        this.availableBikeCount = availableBikeCount;
        this.selfUrl = selfUrl;
        this.hireUrl = hireUrl;
    }

    @JsonProperty("name")
    @NotBlank
    private String name;

    @JsonProperty("location")
    private Location location;

    @JsonProperty("availableBikes")
    private Set<String> availableBikes;

    @JsonProperty("availableBikeCount")
    private Integer availableBikeCount;

    @JsonProperty("selfUrl")
    private String selfUrl;

    @JsonProperty("hireUrl")
    private String hireUrl;

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

    public Set<String> getAvailableBikes() {
        return availableBikes;
    }

    public void setAvailableBikes(Set<String> availableBikes) {
        this.availableBikes = availableBikes;
    }

    public Integer getAvailableBikeCount() {
        return availableBikeCount;
    }

    public void setAvailableBikeCount(Integer availableBikeCount) {
        this.availableBikeCount = availableBikeCount;
    }

    public String getSelfUrl() {
        return selfUrl;
    }

    public void setSelfUrl(String selfUrl) {
        this.selfUrl = selfUrl;
    }

    public String getHireUrl() {
        return hireUrl;
    }

    public void setHireUrl(String hireUrl) {
        this.hireUrl = hireUrl;
    }

    @Override
    public String toString() {
        return "Station{" +
                "name='" + name + '\'' +
                ", location=" + location +
                ", availableBikes=" + availableBikes +
                ", availableBikeCount=" + availableBikeCount +
                ", selfUrl='" + selfUrl + '\'' +
                ", hireUrl='" + hireUrl + '\'' +
                '}';
    }
}
