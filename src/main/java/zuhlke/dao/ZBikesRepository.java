package zuhlke.dao;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.Transaction;
import org.skife.jdbi.v2.sqlobject.customizers.Mapper;
import zuhlke.model.Depleted;
import zuhlke.model.Location;
import zuhlke.model.Station;
import zuhlke.model.mappers.StationCount;
import zuhlke.model.mappers.StationMapper;
import zuhlke.model.mappers.StationWithBikesMapper;

import javax.ws.rs.core.Response.Status;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static javax.ws.rs.core.Response.Status.CONFLICT;
import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.OK;

public abstract class ZBikesRepository {

    @SqlUpdate("delete from stations")
    public abstract void deleteAll();

    @Transaction
    public void upsert(Integer stationId, Station station) {
        //Remove all bikes, wherever they are
        station.getAvailableBikes().forEach(bikeId -> removeBikeById(Integer.parseInt(bikeId)));

        //Remove bikes in current station
        removeBikeByStation(stationId);

        //Create station if it does not exist
        Location location = station.getLocation();
        if (existStation(stationId)) {
            update(stationId, station.getName(), location.getLatitude(), location.getLongitude());
        } else {
            insert(stationId, station.getName(), location.getLatitude(), location.getLongitude());
        }

        //Place bikes in new/updated station
        station.getAvailableBikes().forEach(bikeId -> insertBike(Integer.parseInt(bikeId), stationId));
    }

    @SqlUpdate("update stations set name=:name, lat=:lat, long=:long where station_id=:station_id")
    public abstract void update(@Bind("station_id") Integer station_id, @Bind("name") String name, @Bind("lat") Float lat, @Bind("long") Float lon);

    @SqlUpdate("insert into stations (station_id, name, lat, long) values (:station_id, :name, :lat, :long)")
    public abstract void insert(@Bind("station_id") Integer station_id, @Bind("name") String name, @Bind("lat") Float lat, @Bind("long") Float lon);

    @SqlQuery("select 1 from stations where station_id=:station_id")
    //The returned value must be a primitive (no autoboxing) if no mapper is provided
    public abstract boolean existStation(@Bind("station_id") Integer station_id);

    @SqlQuery("with no_hired as (select * from bikes where hired=FALSE and station_id=:station_id) \\" +
            "select * from stations left join no_hired on (stations.station_id=no_hired.station_id) where stations.station_id=:station_id")
    @Mapper(StationWithBikesMapper.class)
    //For JDBI to loop over multiple rows, the returned value must be a set
    public abstract Set<Station> get(@Bind("station_id") Integer stationId);

    @SqlUpdate("insert into bikes (bike_id, station_id) values (:bike_id, :station_id)")
    public abstract void insertBike(@Bind("bike_id") Integer bikeId, @Bind("station_id") Integer stationId);

    @SqlUpdate("delete from bikes where bike_id=:bike_id")
    public abstract void removeBikeById(@Bind("bike_id") Integer bikeId);

    @SqlUpdate("delete from bikes where station_id=:station_id")
    public abstract void removeBikeByStation(@Bind("station_id") Integer stationId);

    @SqlQuery("select * from stations natural inner join bikes where (lat between :lat1 and :lat2) and (long between :long1 and :long2)")
    @Mapper(StationMapper.class)
    public abstract Set<Station> near(@Bind("lat1") BigDecimal lat1, @Bind("lat2") BigDecimal lat2, @Bind("long1") BigDecimal lon1, @Bind("long2") BigDecimal lon2);

    @SqlUpdate("update bikes set hired=TRUE, hirer=:username where bike_id=:bike_id")
    public abstract void hireBike(@Bind("bike_id") Integer bikeId, @Bind("username") String username);

    @SqlQuery("select 1 from bikes where bike_id=:bike_id")
    public abstract boolean existBike(@Bind("bike_id") Integer bikeId);

    @SqlQuery("select 1 from bikes where bike_id=:bike_id and hired=TRUE")
    protected abstract boolean isHired(@Bind("bike_id") Integer bikeId);

    @SqlQuery("select 1 from bikes where bike_id=:bike_id and hired=TRUE and hirer=:username")
    protected abstract boolean isHiredByUsername(@Bind("bike_id") Integer bikeId, @Bind("username") String username);

    @Transaction
    public Optional<String> hire(Integer stationId, String username) {
        Set<Station> stations = get(stationId);
        return stations.isEmpty() ? empty() :
                stations.iterator().next().getAvailableBikes().stream().findFirst().map(firstBike -> {
                    hireBike(Integer.parseInt(firstBike), username);
                    return of(firstBike);
                }).orElseGet(() -> empty());
    }

    @Transaction
    public Status returnBike(Integer bikeId, Integer stationId, String username) {
        if (!existBike(bikeId)) return NOT_FOUND;
        if (!isHired(bikeId)) return CONFLICT;
        if (!isHiredByUsername(bikeId, username)) return FORBIDDEN;

        //Remove the bike, wherever it is
        removeBikeById(bikeId);
        //Place bike in new station
        insertBike(bikeId, stationId);
        return OK;
    }

    @Transaction
    public Set<Depleted> depleted() {
        Set<Depleted> bikesCount = countBikes();

        return bikesCount.stream().map(depletedStation -> {
            Float lat = depletedStation.getLatitude();
            Float lon = depletedStation.getLongitude();
            Set<Depleted> closestStations = closeStations(depletedStation.getStationId(),
                    new BigDecimal(format("%.2f", (lat - 0.01))), new BigDecimal(format("%.2f", (lat + 0.01))),
                    new BigDecimal(format("%.2f", (lon - 0.01))), new BigDecimal(format("%.2f", (lon + 0.01))));
            depletedStation.setNearbyFullStations(closestStations);
            return depletedStation;
        }).collect(Collectors.toSet());
    }

    @SqlQuery("select count(*),station_id,lat,long \\" +
            "from bikes natural inner join stations \\" +
            "group by station_id,lat,long having count(*)<4")
    @Mapper(StationCount.class)
    public abstract Set<Depleted> countBikes();

    @SqlQuery("select count(*),station_id,lat,long \\" +
            "from bikes natural inner join stations \\ " +
            "where station_id!=:station_id and (lat between :lat1 and :lat2) and (long between :long1 and :long2) \\" +
            "group by station_id,lat,long")
    @Mapper(StationCount.class)
    public abstract Set<Depleted> closeStations(@Bind("station_id") Integer stationId, @Bind("lat1") BigDecimal lat1, @Bind("lat2") BigDecimal lat2, @Bind("long1") BigDecimal lon1, @Bind("long2") BigDecimal lon2);

}
