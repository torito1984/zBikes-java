package zuhlke.resources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zuhlke.dao.ZBikesDao;

import javax.ws.rs.Path;

@Path("/")
public class ZBikesResource {

    private final Logger logger = LoggerFactory.getLogger(ZBikesResource.class);
    private final ZBikesDao zBikesDao;

    public ZBikesResource(ZBikesDao zBikesDao) {
        this.zBikesDao = zBikesDao;
    }

}
