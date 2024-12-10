package space.heartfullmind.relations.rest;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakTransaction;
import space.heartfullmind.relations.data.FriendsRequestsDTO;
import space.heartfullmind.relations.jpa.entity.FriendsRequestsEntity;
import space.heartfullmind.relations.jpa.service.FriendsRequestsService;
import space.heartfullmind.relations.jpa.service.RelationshipService;
import space.heartfullmind.relations.jpa.enums.RelationshipStatus;
import space.heartfullmind.relations.jpa.enums.RelationshipType;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/admin/api/friends-requests")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class FriendsRequestsResource {

    private final RelationshipService relationshipService;
    private final FriendsRequestsService friendsRequestsService;

    private static final Logger log = LoggerFactory.getLogger(FriendsRequestsResource.class);

    public FriendsRequestsResource() {
        this.relationshipService = new RelationshipService();
        this.friendsRequestsService = new FriendsRequestsService();
    }

    @POST
    @Path("/request")
    @Transactional
    public Response sendRequest(FriendsRequestsDTO request) {
        try {
            this.friendsRequestsService.createRequest(request.getUserId(), request.getRelatedUserId());
            return Response.ok()
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }catch (Exception e) {
            e.printStackTrace(); // Add this to see the actual error
            return Response.serverError()
                    .type(MediaType.APPLICATION_JSON)
                    .entity(e.getMessage())
                    .build();
        }

    }

    @PUT
    @Path("/accept/{userId}/{relatedUserId}")
    @Transactional
    public Response acceptFriendRequest(@PathParam("userId") String userId, @PathParam("relatedUserId") String relatedUserId) {
        try {
            this.friendsRequestsService.deleteRequest(userId, relatedUserId);
            this.relationshipService.createRelationship(userId, relatedUserId, RelationshipType.NONE, RelationshipStatus.ACCEPTED);
            return Response.ok()
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }catch (Exception e) {
            return Response.serverError().build();
        }
    }

    @PUT
    @Path("/decline/{userId}/{relatedUserId}")
    @Transactional
    public Response declineFriendRequest(@PathParam("userId") String userId, @PathParam("relatedUserId") String relatedUserId) {
        try {
            this.friendsRequestsService.deleteRequest(userId, relatedUserId);
            return Response.ok()
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }catch (Exception e) {
            return Response.serverError().build();
        }
    }

    @GET
    @Path("/requests/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPendingRequests(@PathParam("userId") String userId) {
        try {
            List<FriendsRequestsEntity> requests = this.friendsRequestsService.getPendingRequests(userId);
            if (requests == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            List<FriendsRequestsDTO> dtos = requests.stream()
                    .map(FriendsRequestsDTO::from)
                    .collect(Collectors.toList());

            return Response.ok(dtos).build();
        } catch (Exception e) {
            log.error("Error getting pending requests", e);
            return Response.serverError().build();
        }
    }
}
