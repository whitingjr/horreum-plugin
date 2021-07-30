package jenkins.plugins.horreum_upload.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.client.jaxrs.internal.ClientResponse;

import io.hyperfoil.tools.yaup.json.Json;
import jenkins.plugins.horreum_upload.api.dto.Access;
import jenkins.plugins.horreum_upload.api.dto.Run;


@Path("/api/run")
@Consumes({MediaType.APPLICATION_JSON})
@Produces(MediaType.APPLICATION_JSON)
public interface HorreumRunService {

	@GET
	@Path("{id}")
	ClientResponse getRun(@PathParam("id") Integer id,
					@QueryParam("token") String token);

	@GET
	@Path("{id}/data")
	ClientResponse getData(@PathParam("id") Integer id, @QueryParam("token") String token);

	@POST
	@Path("{id}/resetToken")
	ClientResponse resetToken(@PathParam("id") Integer id);

	@POST
	@Path("{id}/dropToken")
	ClientResponse dropToken(@PathParam("id") Integer id);

	@POST
	@Path("{id}/updateAccess")
	ClientResponse updateAccess(@PathParam("id") Integer id,
								 @QueryParam("owner") String owner,
								 @QueryParam("access") Access access);

	@GET
	@Path("{id}/structure")
	Json getStructure(@PathParam("id") Integer id,
							 @QueryParam("token") String token);

	@POST
	@Path("test/{test}")
	@Consumes(MediaType.APPLICATION_JSON)
	ClientResponse add(@PathParam("test") String testNameOrId,
						@QueryParam("owner") String owner,
						@QueryParam("access") Access access,
						@QueryParam("token") String token,
						Run run);

	@POST
	@Path("data")
	ClientResponse addRunFromData(@QueryParam("start") String start,
								   @QueryParam("stop") String stop,
								   @QueryParam("test") String test,
								   @QueryParam("owner") String owner,
								   @QueryParam("access") Access access,
								   @QueryParam("schema") String schemaUri,
								   @QueryParam("description") String description,
								   @QueryParam("token") String token,
								   String data);

	@GET
	@Path("autocomplete")
	ClientResponse autocomplete(@QueryParam("query") String query);

	@GET
	@Path("list")
	Json list(@QueryParam("query") String query,
					 @QueryParam("matchAll") boolean matchAll,
					 @QueryParam("roles") String roles,
					 @QueryParam("trashed") boolean trashed,
					 @QueryParam("limit") Integer limit,
					 @QueryParam("page") Integer page,
					 @QueryParam("sort") String sort,
					 @QueryParam("direction") String direction);

	@GET
	@Path("count")
	ClientResponse runCount(@QueryParam("testId") Integer testId);

	@GET
	@Path("list/{testId}/")
	ClientResponse testList(@PathParam("testId") Integer testId,
							 @QueryParam("limit") Integer limit,
							 @QueryParam("page") Integer page,
							 @QueryParam("sort") String sort,
							 @QueryParam("direction") String direction,
							 @QueryParam("trashed") boolean trashed,
							 @QueryParam("tags") String tags
	);

	@POST
	@Path("{id}/trash")
	ClientResponse trash(@PathParam("id") Integer id, @QueryParam("isTrashed") Boolean isTrashed);

	@POST
	@Path("{id}/description")
	@Consumes(MediaType.TEXT_PLAIN)
	ClientResponse updateDescription(@PathParam("id") Integer id, String description);

	@POST
	@Path("{id}/schema")
	@Consumes(MediaType.TEXT_PLAIN)
	ClientResponse updateSchema(@PathParam("id") Integer id, @QueryParam("path") String path, String schemaUri);
}
