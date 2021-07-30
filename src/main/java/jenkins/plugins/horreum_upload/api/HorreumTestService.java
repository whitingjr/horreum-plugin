package jenkins.plugins.horreum_upload.api;

import java.util.Collection;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.client.jaxrs.internal.ClientResponse;

import jenkins.plugins.horreum_upload.api.dto.Access;
import jenkins.plugins.horreum_upload.api.dto.Hook;
import jenkins.plugins.horreum_upload.api.dto.HorreumTest;
import jenkins.plugins.horreum_upload.api.dto.Sort;
import jenkins.plugins.horreum_upload.api.dto.TestToken;
import jenkins.plugins.horreum_upload.api.dto.View;


@Path("/api/test")
@Consumes({MediaType.APPLICATION_JSON})
@Produces(MediaType.APPLICATION_JSON)
public interface HorreumTestService {

	@DELETE
	@Path("{id}")
	void delete(@PathParam("id") Integer id);

	@GET
	@Path("{id}")
	HorreumTest get(@PathParam("id") Integer id, @QueryParam("token") String token);

	@POST
	ClientResponse add(HorreumTest test);


	@GET
	List<HorreumTest> list(@QueryParam("roles") String roles,
						   @QueryParam("limit") Integer limit,
						   @QueryParam("page") Integer page,
						   @QueryParam("sort") @DefaultValue("name") String sort,
						   @QueryParam("direction") @DefaultValue("Ascending") Sort.Direction direction);

	@Path("summary")
	@GET
	ClientResponse summary(@QueryParam("roles") String roles);

	@POST
	@Path("{id}/addToken")
	ClientResponse addToken(@PathParam("id") Integer testId, TestToken token);

	@GET
	@Path("{id}/tokens")
	Collection<TestToken> tokens(@PathParam("id") Integer testId);

	@POST
	@Path("{id}/revokeToken/{tokenId}")
	ClientResponse dropToken(@PathParam("id") Integer testId, @PathParam("tokenId") Integer tokenId);

	@POST
	@Path("{id}/updateAccess")
	ClientResponse updateAccess(@PathParam("id") Integer id,
								 @QueryParam("owner") String owner,
								 @QueryParam("access") Access access);

	@POST
	@Path("{testId}/view")
	ClientResponse updateView(@PathParam("testId") Integer testId, View view);

	@POST
	@Consumes // any
	@Path("{id}/notifications")
	ClientResponse updateAccess(@PathParam("id") Integer id,
								 @QueryParam("enabled") boolean enabled);

	@POST
	@Path("{testId}/hook")
	ClientResponse updateHook(@PathParam("testId") Integer testId, Hook hook);

	@GET
	@Path("{id}/tags")
	ClientResponse tags(@PathParam("id") Integer testId, @QueryParam("trashed") Boolean trashed);
}
