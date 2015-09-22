package zuhlke.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotBlank;

import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Depleted {

    public Depleted() { }

    public Depleted(Integer stationId, String stationUrl, Integer availableBikes, Set<Depleted> nearbyFullStations) {
        this.stationId = stationId;
        this.stationUrl = stationUrl;
        this.availableBikes = availableBikes;
        this.nearbyFullStations = nearbyFullStations;
    }

    @JsonIgnore
    private Integer stationId;

    @JsonIgnore
    private Float latitude;

    @JsonIgnore
    private Float longitude;

    @JsonProperty("stationUrl")
    @NotBlank
    private String stationUrl;

    @JsonProperty("availableBikes")
    @NotBlank
    private Integer availableBikes;

    @JsonProperty("nearbyFullStations")
    private Set<Depleted> nearbyFullStations;

    public Integer getStationId() {
        return stationId;
    }

    public void setStationId(Integer stationId) {
        this.stationId = stationId;
    }

    public Float getLatitude() {
        return latitude;
    }

    public void setLatitude(Float latitude) {
        this.latitude = latitude;
    }

    public Float getLongitude() {
        return longitude;
    }

    public void setLongitude(Float longitude) {
        this.longitude = longitude;
    }

    public String getStationUrl() {
        return stationUrl;
    }

    public void setStationUrl(String stationUrl) {
        this.stationUrl = stationUrl;
    }

    public Integer getAvailableBikes() {
        return availableBikes;
    }

    public void setAvailableBikes(Integer availableBikes) {
        this.availableBikes = availableBikes;
    }

    public Set<Depleted> getNearbyFullStations() {
        return nearbyFullStations;
    }

    public void setNearbyFullStations(Set<Depleted> nearbyFullStations) {
        this.nearbyFullStations = nearbyFullStations;
    }

    @Override
    public String toString() {
        return "Depleted{" +
                "stationId=" + stationId +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", stationUrl='" + stationUrl + '\'' +
                ", availableBikes=" + availableBikes +
                ", nearbyFullStations=" + nearbyFullStations +
                '}';
    }
}
