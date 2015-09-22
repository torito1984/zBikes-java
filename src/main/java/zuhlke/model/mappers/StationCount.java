package zuhlke.model.mappers;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;
import zuhlke.model.Depleted;

import java.sql.ResultSet;
import java.sql.SQLException;

public class StationCount implements ResultSetMapper<Depleted> {

    @Override
    public Depleted map(int index, ResultSet r, StatementContext ctx) throws SQLException {
        Depleted d = new Depleted();
        d.setAvailableBikes(r.getInt("count"));
        d.setStationUrl("/station/"+r.getString("station_id"));

        d.setStationId(r.getInt("station_id"));
        d.setLatitude(r.getFloat("lat"));
        d.setLongitude(r.getFloat("long"));

        return d;
    }
}