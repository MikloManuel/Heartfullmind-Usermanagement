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
import space.heartfullmind.relations.data.FriendsRequestsDTO;
import space.heartfullmind.relations.jpa.service.FriendsRequestsService;
import space.heartfullmind.relations.jpa.service.RelationshipService;
import space.heartfullmind.relations.jpa.enums.RelationshipStatus;
import space.heartfullmind.relations.jpa.enums.RelationshipType;
import org.jboss.logging.Logger;
import space.heartfullmind.relations.jpa.entity.FriendsRequestsEntity;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Path("/admin/api/friends-requests")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class FriendsRequestsResource {
    private static final Logger log = Logger.getLogger(FriendsRequestsResource.class);
    @Context
    private final KeycloakSession session;
    private final RelationshipService relationshipService;
    private final FriendsRequestsService friendsRequestsService;

    public FriendsRequestsResource(KeycloakSession session) {
        this.relationshipService = new RelationshipService(session);
        this.friendsRequestsService = new FriendsRequestsService(session);
        this.session = session;
    }

    @POST
    @Path("/request")
    @Transactional
    public Response sendFriendRequest(FriendsRequestsDTO request) {
        try {
            // relationshipService.createRelationship(request.getUserId(), request.getRelatedUserId(), RelationshipType.NONE, RelationshipStatus.PENDING);
            friendsRequestsService.createRequest(request.getUserId(), request.getRelatedUserId());
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
        log.debugf("Accepting friend request: userId=%s, relatedUserId=%s", userId, relatedUserId);
        try {
            friendsRequestsService.deleteRequest(userId, relatedUserId);
            relationshipService.createRelationship(userId, relatedUserId, RelationshipType.NONE, RelationshipStatus.ACCEPTED);
            return Response.ok()
                    .type(MediaType.APPLICATION_JSON)
                    .entity("{\"status\": \"success\"}")  // Add response body
                    .build();
        } catch (Exception e) {
            return Response.serverError()
                    .type(MediaType.APPLICATION_JSON)
                    .entity("{\"error\": \"" + e.getMessage() + "\"}")
                    .build();
        }
    }

    @PUT
    @Path("/decline/{userId}/{relatedUserId}")
    @Transactional
    public Response declineFriendRequest(@PathParam("userId") String userId, @PathParam("relatedUserId") String relatedUserId) {
        try {
            friendsRequestsService.deleteRequest(userId, relatedUserId);
            return Response.ok()
                    .type(MediaType.APPLICATION_JSON)
                    .entity("{\"status\": \"success\"}")  // Add response body
                    .build();
        } catch (Exception e) {
            return Response.serverError()
                    .type(MediaType.APPLICATION_JSON)
                    .entity("{\"error\": \"" + e.getMessage() + "\"}")
                    .build();
        }
    }


    @GET
    @Path("/requests/{userId}")
    @Transactional
    public List<FriendsRequestsDTO> getPendingRequests(@PathParam("userId") String userId) {
        log.debugf("Getting pending requests for user: %s", userId); // Fix debug format
        try {
            List<FriendsRequestsEntity> requests = friendsRequestsService.getPendingRequests(userId);
            log.debugf("Found %d pending requests", requests.size()); // Fix debug format

            List<FriendsRequestsDTO> dtos = requests.stream()
                    .map(FriendsRequestsDTO::from)  // Use method reference instead
                    .collect(Collectors.toList());
            // List<FriendsRequestsDTO> dtos = requests.stream();
            return dtos;
        } catch (Exception e) {
            log.errorf("Error getting pending requests for user %s: %s", userId, e.getMessage()); // Fix error format
            return Collections.emptyList();
        }
    }


}
