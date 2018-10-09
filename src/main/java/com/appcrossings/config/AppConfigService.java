package com.appcrossings.config;


import java.io.InputStream;
import java.util.Set;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/v1")
public interface AppConfigService {

  @GET
  @Path("/health")
  @Consumes(MediaType.WILDCARD)
  @Produces(MediaType.WILDCARD)
  Response getHealth();

  /**
   * Traverse the path to the root of the repo and return merged properties.
   * 
   * @param repo Optional repo name to specify for search otherwise default repo is used
   * 
   * @param path The path to start traversal
   * 
   * @param traverse Flag whether or not to just fetch properties at the indicated path or to start
   *        traversal at that path. Default = traverse
   * @return
   */
  @GET
  @Path("/{path:.{0,}}")
  @Consumes({MediaType.WILDCARD})
  @Produces({MediaType.APPLICATION_JSON})
  Response getJsonProperties(@QueryParam("repo") String repo, @PathParam("path") String path,
      @DefaultValue("true") @QueryParam("t") Boolean traverse, @QueryParam("p") Set<String> named);


  /**
   * Traverse the path to the root of the repo and return merged properties.
   * 
   * @param repo Optional repo name to specify for search otherwise default repo is used
   * 
   * @param path The path to start traversal
   * 
   * @param traverse Flag whether or not to just fetch properties at the indicated path or to start
   *        traversal at that path. Default = traverse
   * @return
   */
  @GET
  @Path("/{path:.{0,}}")
  @Consumes({MediaType.WILDCARD})
  @Produces({MediaType.TEXT_PLAIN})
  Response getTextProperties(@QueryParam("repo") String repo, @PathParam("path") String path,
      @DefaultValue("true") @QueryParam("t") Boolean traverse, @QueryParam("p") Set<String> named);

  /**
   * Traverse the path to the root of the repo and return merged properties.
   * 
   * @param repo Optional repo name to specify for search otherwise default repo is used
   * 
   * @param path The path to start traversal
   * 
   * @param traverse Flag whether or not to just fetch properties at the indicated path or to start
   *        traversal at that path. Default = traverse
   * @return
   */
  @GET
  @Path("/{path:.{0,}}")
  @Consumes({MediaType.WILDCARD})
  @Produces({"application/x-yam"})
  Response getYamlProperties(@QueryParam("repo") String repo, @PathParam("path") String path,
      @DefaultValue("true") @QueryParam("t") Boolean traverse, @QueryParam("p") Set<String> named)
      throws Exception;

  /**
   * Traverse the path to the root of the repo and return merged properties.
   * 
   * @param repo Optional repo name to specify for search otherwise default repo is used
   * 
   * @param path The path to start traversal
   * 
   * @param traverse Flag whether or not to just fetch properties at the indicated path or to start
   *        traversal at that path. Default = traverse
   * @return
   */
  @PUT
  @Path("/{path:.{0,}}")
  @Consumes({MediaType.TEXT_PLAIN})
  @Produces({MediaType.TEXT_PLAIN})
  Response putTextProperties(@QueryParam("repo") String repo, @PathParam("path") String path,
      @HeaderParam("Etag") String etag, InputStream body) throws Exception;

}
