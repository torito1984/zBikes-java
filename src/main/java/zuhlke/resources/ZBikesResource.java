package zuhlke.resources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zuhlke.dao.ZBikesRepository;
import zuhlke.model.Station;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Set;

import static java.lang.String.format;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.created;
import static javax.ws.rs.core.Response.ok;
import static javax.ws.rs.core.Response.status;

@Path("/")
public class ZBikesResource {

    private final Logger logger = LoggerFactory.getLogger(ZBikesResource.class);
    private final ZBikesRepository zBikesRepository;

    public ZBikesResource(ZBikesRepository zBikesDao) {
        this.zBikesRepository = zBikesDao;
    }

    @DELETE
    @Path("/station/all")
    public Response deleteAll() {
        zBikesRepository.deleteAll();
        return ok().build();
    }

    @GET
    @Path("/station/{stationId}")
    @Produces(APPLICATION_JSON)
    public Response getStation(@PathParam("stationId") Integer stationId) {
        Set<Station> stations = zBikesRepository.get(stationId);
        return stations.isEmpty()?
                status(NOT_FOUND).entity("Station not found for ID: " + stationId).build() : ok(stations.toArray()[0]).build();
    }

    @PUT
    @Path("/station/{stationId}")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    public Response upsert(@PathParam("stationId") Integer stationId, @Context UriInfo uriInfo, @Valid Station station) {
        zBikesRepository.upsert(stationId, station);
        return created(uriInfo.getRequestUri()).entity(station).build();
    }

    @GET
    @Path("/station/near/{lat}/{lon}")
    @Produces(APPLICATION_JSON)
    public Response getClosestStations(@PathParam("lat") Float lat, @PathParam("lon") Float lon) {

        Set<Station> nearStations = zBikesRepository.near(
                        new BigDecimal(format("%.2f", (lat - 0.01))), new BigDecimal(format("%.2f", (lat + 0.01))),
                        new BigDecimal(format("%.2f", (lon - 0.01))), new BigDecimal(format("%.2f", (lon + 0.01)))
        );

        return ok(new HashMap<String, Object>(){{
            put("items", nearStations);
        }}).build();
    }
}
