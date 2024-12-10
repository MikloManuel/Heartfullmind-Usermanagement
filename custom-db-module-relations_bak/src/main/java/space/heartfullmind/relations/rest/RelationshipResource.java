package space.heartfullmind.relations.rest;

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
import org.keycloak.models.KeycloakTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.heartfullmind.relations.data.RelationshipDTO;
import space.heartfullmind.relations.jpa.enums.RelationshipStatus;
import space.heartfullmind.relations.jpa.enums.RelationshipType;
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
    private static final Logger log = LoggerFactory.getLogger(RelationshipResource.class);


    public RelationshipResource() {
        this.relationshipService = new RelationshipService();
    }

    @GET
    @Path("/relations/{userId}")
    public List<RelationshipDTO> getRelations(@PathParam("userId") String userId) {
        try {
            List<RelationshipDTO> relations = relationshipService.getRelationships(userId);
            return relations;
        } catch (Exception e) {
            return List.of();
        }
    }

    @GET
    @Path("/relations/update/{userId}/{relatedUserId}/{relationshipStatus}/{relationshipType}")
    public List<RelationshipDTO> setRelationshipStatus(@PathParam("userId") String userId, @PathParam("relatedUserId") String relatedUserId, @PathParam("relationshipStatus") RelationshipStatus relationshipStatus, @PathParam("relationshipType") RelationshipType relationshipType) {
        try {
            return (List<RelationshipDTO>) relationshipService.setRelationshipStatus(userId, relatedUserId, relationshipStatus, relationshipType );
        }catch (Exception e) {
            return List.of();
        }
    }

    @POST
    @Path("/relationship")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createRelationship(RelationshipDTO relationship) {
        log.info("Creating relationship: {}", relationship);

        try {
            KeycloakTransaction transaction = session.getTransactionManager();
            if (!transaction.isActive()) {
                transaction.begin();
            }

            try {
                relationshipService.createRelationship(
                        relationship.getUserId(),
                        relationship.getRelatedUserId(),
                        relationship.getRelationshipType(),
                        RelationshipStatus.ACCEPTED  // Since this is a direct relationship creation
                );

                transaction.commit();
                log.info("Successfully created bidirectional relationship between {} and {}",
                        relationship.getUserId(), relationship.getRelatedUserId());

                return Response.ok().build();

            } catch (Exception e) {
                transaction.rollback();
                log.error("Error creating relationship: ", e);
                throw e;
            }
        } catch (Exception e) {
            log.error("Failed to create relationship: ", e);
            return Response.serverError()
                    .entity("Failed to create relationship: " + e.getMessage())
                    .build();
        }
    }




    @DELETE
    @Path("/delete/{userId}/{relatedUserId}")
    public void deleteRelationship(@PathParam("userId") String userId, @PathParam("relatedUserId") String relatedUserId) {
        relationshipService.deleteRelationship(userId, relatedUserId);
    }
}
