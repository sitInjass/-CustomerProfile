package bop.src.main;

import bop.src.main.annotation.AdminOperation;
import bop.src.main.bean.DatabaseTimeData;
import bop.src.main.bean.FullNameRequest;
import bop.src.main.bean.FullNameRespons;
import bop.src.main.config.PermissionCodes;
import bop.src.main.service.DatabaseTimeService;
import bop.src.main.service.FullNameService;
import bop.src.main.util.CentralServicesClient;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;


/**
 * 
 * @author ِAshraf.M.Fahmawi
 */

@Path("system")
public class SystemResource {

    private final DatabaseTimeService databaseTimeService;
    private final FullNameService fullNameService;

    public SystemResource() {
        this.databaseTimeService
                = new DatabaseTimeService();
        this.fullNameService
                = new FullNameService();
    }

    /*
     * Public endpoint.
     * No token or permission is required.
     */
    @GET
    @Path("/health")
    @Produces(MediaType.TEXT_PLAIN)
    public String health(@Context HttpServletRequest httpRequest) {

        CentralServicesClient centralClient = new CentralServicesClient();
        return "System services are up and running " + centralClient.getAuthUserId(httpRequest);
    }

    @GET
    @Path("/databaseTime")
    @Produces(MediaType.APPLICATION_JSON)
    @AdminOperation(
            permission = PermissionCodes.DATABASE_TIME_VIEW
    )
    public DatabaseTimeData getDatabaseTime(@Context HttpServletRequest httpRequest)
            throws Exception {

        CentralServicesClient centralClient = new CentralServicesClient();
        System.out.println(centralClient.getAuthUserId(httpRequest));
        return databaseTimeService.getDatabaseTime();
    }

    @POST
    @Path("/buildFullName")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @AdminOperation(
            permission = PermissionCodes.FULL_NAME_CREATE
    )
    public FullNameRespons buildFullName(
            FullNameRequest request, @Context HttpServletRequest httpRequest)
            throws Exception {

        CentralServicesClient centralClient = new CentralServicesClient();
        System.out.println(centralClient.getAuthUserId(httpRequest));
        return fullNameService.buildFullName(request);
    }
}
