package space.heartfullmind.relations.rest;

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

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Path("/admin/api/friends-requests")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class FriendsRequestsResource {

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
    public Response sendRequest(FriendsRequestsDTO request) {
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
    public Response acceptFriendRequest(@PathParam("userId") String userId, @PathParam("relatedUserId") String relatedUserId) {
        try {
            friendsRequestsService.deleteRequest(userId, relatedUserId);
            relationshipService.createRelationship(userId, relatedUserId, RelationshipType.NONE, RelationshipStatus.ACCEPTED);
            return Response.ok()
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }catch (Exception e) {
            return Response.serverError().build();
        }
    }

    @PUT
    @Path("/decline/{userId}/{relatedUserId}")
    public Response declineFriendRequest(@PathParam("userId") String userId, @PathParam("relatedUserId") String relatedUserId) {
        try {
            friendsRequestsService.deleteRequest(userId, relatedUserId);
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
    @Consumes(MediaType.APPLICATION_JSON)
    public List<FriendsRequestsDTO> getPendingRequests(@PathParam("userId") String userId) {
        try {
            return friendsRequestsService.getPendingRequests(userId)
                    .stream()
                    .map(FriendsRequestsDTO::from)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
