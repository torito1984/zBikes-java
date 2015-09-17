package zuhlke.model.mappers;

import org.apache.commons.lang3.StringUtils;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;
import zuhlke.model.Location;
import zuhlke.model.Station;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class StationMapper implements ResultSetMapper<Station> {

    private Station station;

    //This method will be called once per retrieved row
    //It will not be called it there are not rows retrieved
    @Override
    public Station map(int index, ResultSet r, StatementContext ctx) throws SQLException {
        if (index == 0) {
            Location location = new Location(r.getFloat("lat"), r.getFloat("long"));
            String name = r.getString("name");
            station = new Station(name, location, new ArrayList<>());
        }
        int bike_id = r.getInt("bike_id");
        if(bike_id>0) {
            station.getAvailableBikes().add(StringUtils.leftPad(Integer.toString(bike_id), 3, "0"));
        }
        return station;
    }
}