import {
  Alert,
  Badge,
  PageSection,
  Button,
  DataList,
  DataListItem,
  DataListItemRow,
  DataListItemCells,
  DataListCell,
  DataListCheck,
  Popover,
} from "@patternfly/react-core";
import { useState, useEffect } from "react";
import { FriendsRequestsService } from "../FriendsRequestsService";
import { useAlerts } from "@keycloak/keycloak-ui-shared";
import { AlertVariant } from "@patternfly/react-core";
import { useTranslation } from "react-i18next";
import UserRepresentation from "js/libs/keycloak-admin-client/lib/defs/userRepresentation";
import { useRealm } from "../../../context/realm-context/RealmContext";
import { FriendsRequestsRepresentation } from "../data/FriendsRequestsRepresentation";
import { RelationshipService } from "../../relations/RelationshipService";

export const FriendsRequestsTab = ({
  currentUser,
  users,
}: {
  currentUser: UserRepresentation;
  users: UserRepresentation[];
}) => {
  const { t } = useTranslation();
  const { addAlert, addError } = useAlerts();
  const { realmRepresentation: realm } = useRealm();
  const serverUrl = realm?.attributes?.["serverUrl"] || window.location.origin;
  const [selectedUsers, setSelectedUsers] = useState<UserRepresentation[]>([]);
  const friendsRequestsService = new FriendsRequestsService(serverUrl);
  const relationshipService = new RelationshipService(serverUrl);
  const [pendingRequests, setPendingRequests] = useState<
    FriendsRequestsRepresentation[]
  >([]);
  const [sentRequests, setSentRequests] = useState<Set<string>>(new Set());

  useEffect(() => {
    const fetchRelations = async () => {
      try {
        // Get existing relations
        const relations = await relationshipService.getRelations(currentUser.id!);
        const connectedUsers = users.filter(user =>
            relations.some(relation =>
                (relation.userId === user.id || relation.relatedUserId === user.id) &&
                relation.relationshipStatus === "ACCEPTED"
            )
        );

        // Get pending requests
        const requests = await friendsRequestsService.getFriendsRequests(currentUser.id!);
        setPendingRequests(requests!);
        // Get users from pending requests
        const usersFromRequests = users.filter(user =>
            requests!.some(request =>
                request.userId === user.id || request.relatedUserId === user.id
            )
        );

        // Combine both sets of users
        const allSelectedUsers = [...connectedUsers, ...usersFromRequests];

        // Update state
        setSelectedUsers(allSelectedUsers);
        setPendingRequests(requests!);

      } catch (error) {
        console.error("Error fetching relations:", error);
      }

    };
    fetchRelations().catch((error) => {
      console.error("Error in fetchRelations effect:", error);
    });
  }, [currentUser.id, users]);

  const hasPendingRequest = (userId: string) => {
    console.log('Checking for pending request for userId:', userId);
    return pendingRequests!.some(
      (request: FriendsRequestsRepresentation) =>
        request.userId === userId || request.relatedUserId === userId,
    );
  };

  const isUserDisabled = (userId: string) => {
    const isSelected = selectedUsers?.some(u => u.id === userId);
    const hasPending = pendingRequests?.some(
        req => req.userId === userId || req.relatedUserId === userId
    );
    console.log(`User ${userId} - Selected: ${isSelected}, Pending: ${hasPending}`);
    return isSelected || hasPending;
  };

  const refresh = () => {
    friendsRequestsService
      .getFriendsRequests(currentUser.id!)
      .then((requests) => {
        setPendingRequests(requests!);
      })
      .catch((error) => {
        addError("refresh", error);
      });
  };

  const handleAcceptRequest = (userId: string, relatedUserId: string) => {
    friendsRequestsService
      .acceptFriendRequest(userId, relatedUserId)
      .then(() => {
        relationshipService.updateRelationship(userId, relatedUserId, "ACCEPTED", "FRIENDS").then(() => {
          addAlert(t("friendRequestAccepted"), AlertVariant.success);
          refresh();
        })
            .catch((error) => {
              addError(error.messageKey, error.message);
            });
      })
  };

  const handleDeclineRequest = (userId: string, relatedUserId: string) => {
    friendsRequestsService
      .declineFriendRequest(userId, relatedUserId)
      .then(() => {
        addAlert(t("friendRequestDeclined"), AlertVariant.success);
        refresh();
      })
      .catch((error) => {
        addError(error.messageKey, error.message);
      });
  };

  const handleSendRequest = async (userId: string, targetUserId: string) => {
    try {
      await friendsRequestsService.sendFriendRequest({
        userId: userId,
        relatedUserId: targetUserId
      });
      setSentRequests(prev => new Set([...prev, targetUserId]));
      addAlert(t("friendRequestSent"), AlertVariant.success);
      refresh();
    } catch (error: any) {
      addError(error.messageKey, error.message);
    }
  };



  return (
    <>
      <PageSection variant="light">
        <Popover
          bodyContent={
            selectedUsers && selectedUsers.length > 0 ? (
              <DataList aria-label="Friend requests">
                {selectedUsers!.map((request: any) => (
                  <DataListItem key={request.id}>
                    <DataListItemRow>
                      <DataListItemCells
                        dataListCells={[
                          <DataListCell key="name">
                            {request.username}
                          </DataListCell>,
                          <DataListCell key="actions">
                            <Button
                              variant="primary"
                              onClick={() =>
                                handleAcceptRequest(
                                  currentUser.id!,
                                  request.id!,
                                )
                              }
                            >
                              Accept
                            </Button>
                            <Button
                              variant="danger"
                              onClick={() =>
                                handleDeclineRequest(
                                  currentUser.id!,
                                  request.id!,
                                )
                              }
                            >
                              Decline
                            </Button>
                          </DataListCell>,
                        ]}
                      />
                    </DataListItemRow>
                  </DataListItem>
                ))}
              </DataList>
            ) : null
          }
          triggerAction="hover"
        >
          <div style={{ display: "inline-block", position: "relative" }}>
            <Alert
              variant="info"
              isInline
              title={t("newFriendRequests")}
              className="notification-alert"
              style={{ width: "auto" }}
            >
              <Badge
                style={{ position: "absolute", top: "-10px", right: "-10px" }}
              >
                {pendingRequests!.length}
              </Badge>
            </Alert>
          </div>
        </Popover>
      </PageSection>

      <PageSection>
        <DataList aria-label="Available users">
          {users.map((user) => (
            <DataListItem key={`select-${user.id}`}>
              <DataListItemRow>
                <DataListCheck
                    aria-labelledby={`select-${user.id}`}
                    name={`select-${user.id}`}
                    isChecked={selectedUsers?.includes(user)}
                    isDisabled={isUserDisabled(user.id!)}
                />
                <DataListCell>{user.username}</DataListCell>
                <DataListCell>
                  <Button
                      variant="secondary"
                      isDisabled={hasPendingRequest(user.id!) ||
                          selectedUsers?.some((u) => u.id === user.id) ||
                          sentRequests.has(user.id!)}
                      onClick={() => handleSendRequest(currentUser.id!, user.id!)}
                  >
                    {t("Send Request")}
                  </Button>
                </DataListCell>
              </DataListItemRow>
            </DataListItem>
          ))}
        </DataList>
      </PageSection>
    </>
  );
};
