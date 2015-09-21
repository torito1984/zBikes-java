package zuhlke.model.mappers;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;
import zuhlke.model.Location;
import zuhlke.model.Station;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class StationMapper implements ResultSetMapper<Station> {

    private Map<Integer,Station> stationsById = new HashMap<>();

    @Override
    public Station map(int index, ResultSet r, StatementContext ctx) throws SQLException {
        int station_id = r.getInt("station_id");
        Station station = stationsById.get(station_id);
        if (station==null) {
            Location location = new Location(r.getFloat("lat"), r.getFloat("long"));
            String name = r.getString("name");
            station = new Station(name, location, null, 0, "/station/"+station_id, "/station/"+station_id+"/bike");
            stationsById.put(station_id, station);
        }
        station.setAvailableBikeCount(station.getAvailableBikeCount()+1);
        return station;
    }
}