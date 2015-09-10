package zuhlke.dao;

import org.skife.jdbi.v2.sqlobject.SqlUpdate;

public interface ZBikesDao {

    @SqlUpdate("delete from stations")
    void deleteAll();

}
