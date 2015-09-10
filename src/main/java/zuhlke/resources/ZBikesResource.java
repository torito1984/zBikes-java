package zuhlke.resources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zuhlke.dao.ZBikesDao;

import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("/")
public class ZBikesResource {

    private final Logger logger = LoggerFactory.getLogger(ZBikesResource.class);
    private final ZBikesDao zBikesDao;

    public ZBikesResource(ZBikesDao zBikesDao) {
        this.zBikesDao = zBikesDao;
    }

    @DELETE
    @Path("/station/all")
    public Response deleteAll() {
        zBikesDao.deleteAll();
        return Response.ok().build();
    }

}
