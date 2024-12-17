package space.heartfullmind.relations.rest;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.keycloak.models.KeycloakSession;
import space.heartfullmind.relations.data.RelationshipDTO;
import space.heartfullmind.relations.jpa.service.FriendsRequestsService;
import space.heartfullmind.relations.jpa.service.RelationshipService;

import java.util.List;

@Path("/admin/api/relationships")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RelationshipResource {

    @Context
    private KeycloakSession session;

    private final RelationshipService relationshipService;
    private final FriendsRequestsService friendsRequestsService;

    public RelationshipResource(KeycloakSession session) {
        this.relationshipService = new RelationshipService(session);
        this.friendsRequestsService = new FriendsRequestsService(session);
        this.session = session;
    }

    @GET
    @Path("/relations/{userId}")
    public List<RelationshipDTO> getRelations(@PathParam("userId") String userId) {
    try {
        List<RelationshipDTO> relationships;
        relationships = relationshipService.getRelationships(userId);
        return relationships;
        }catch (Exception e) {
            return List.of();
        }
    }

    @POST
    @Path("/relationship")
    @Transactional
    public Response createRelationship(RelationshipDTO relationship) {
        try {
            relationshipService.createRelationship(
                    relationship.getUserId(),
                    relationship.getRelatedUserId(),
                    relationship.getRelationshipType(),
                    relationship.getRelationshipStatus()
            );
            return Response.ok()
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        } catch (Exception e) {
            e.printStackTrace(); // This will show the full stack trace in the logs
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(e.getMessage())
                    .build();
        }
    }

    @DELETE
    @Path("/delete/{userId}/{relatedUserId}")
    public void deleteRelationship(@PathParam("userId") String userId, @PathParam("relatedUserId") String relatedUserId) {
        relationshipService.deleteRelationship(userId, relatedUserId);
    }
}
