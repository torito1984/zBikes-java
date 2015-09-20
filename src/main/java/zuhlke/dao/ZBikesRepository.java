package zuhlke.dao;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.Transaction;
import org.skife.jdbi.v2.sqlobject.customizers.Mapper;
import zuhlke.model.Location;
import zuhlke.model.Station;
import zuhlke.model.mappers.StationMapper;

import java.util.List;

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
        if (exists(stationId)) {
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
    public abstract boolean exists(@Bind("station_id") Integer station_id);

    @SqlQuery("select * from stations left join bikes on (stations.station_id=bikes.station_id) where stations.station_id=:station_id")
    @Mapper(StationMapper.class)
    //For JDBI to loop over multiple rows, the returned value must be a set, even if all the rows are combined in a single Station
    public abstract List<Station> get(@Bind("station_id") Integer stationId);

    @SqlUpdate("insert into bikes (bike_id, station_id) values (:bike_id, :station_id)")
    public abstract void insertBike(@Bind("bike_id") Integer bikeId, @Bind("station_id") Integer stationId);

    @SqlUpdate("delete from bikes where bike_id=:bike_id")
    public abstract void removeBikeById(@Bind("bike_id") Integer bikeId);

    @SqlUpdate("delete from bikes where station_id=:station_id")
    public abstract void removeBikeByStation(@Bind("station_id") Integer stationId);

}