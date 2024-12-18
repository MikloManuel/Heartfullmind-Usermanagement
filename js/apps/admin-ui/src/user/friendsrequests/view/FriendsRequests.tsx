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
  // @ts-ignore
  const { realmRepresentation } = useRealm();
  const serverUrl =
    realmRepresentation?.attributes?.["serverUrl"] || window.location.origin;
  const [selectedUsers, setSelectedUsers] = useState<UserRepresentation[]>([]);
  const friendsRequestsService = new FriendsRequestsService(serverUrl);
  const relationshipService = new RelationshipService(serverUrl);
  const [pendingRequests, setPendingRequests] = useState<
    FriendsRequestsRepresentation[]
  >([]);
  const [sentRequests, setSentRequests] = useState<Set<string>>(new Set());
  const [checkedUsers, setCheckedUsers] = useState<Set<UserRepresentation>>(
    new Set(),
  );

  useEffect(() => {
    const handleFriendsRequestsUpdate = () => {
      // Refresh relations data
      refresh();
    };

    // Add event listener
    document.addEventListener(
      "friendsRequestsUpdated",
      handleFriendsRequestsUpdate,
    );

    // Cleanup
    return () => {
      document.removeEventListener(
        "friendsRequestsUpdated",
        handleFriendsRequestsUpdate,
      );
    };
  }, []);

  useEffect(() => {
    const newCheckedUsers = pendingRequests
      .map((request) => {
        const userId =
          request.userId === currentUser.id
            ? request.relatedUserId
            : request.userId;
        return users.find((user) => user.id === userId);
      })
      .filter((user): user is UserRepresentation => user !== undefined);

    setCheckedUsers(new Set(newCheckedUsers));
  }, [pendingRequests, users, currentUser.id]);

  useEffect(() => {
    refresh();
  }, [currentUser.id]);

  useEffect(() => {
    const fetchRelations = async () => {
      try {
        // Get existing relations
        const relations = await relationshipService.getRelations(
          currentUser.id!,
        );
        const connectedUsers = users.filter((user) =>
          relations.some(
            (relation) =>
              (relation.userId === user.id ||
                relation.relatedUserId === user.id) &&
              relation.relationshipStatus === "ACCEPTED",
          ),
        );

        // Get ALL pending requests (both sent and received)
        const requests = await friendsRequestsService.getFriendsRequests(
          currentUser.id!,
        );

        // Update sentRequests state with requests sent by current user
        const sentRequestIds = requests!
          .filter((request) => request.userId === currentUser.id)
          .map((request) => request.relatedUserId);
        setSentRequests(new Set(sentRequestIds));

        // Only show received requests in the notification badge
        const receivedRequests = requests!.filter(
          (request) => request.relatedUserId === currentUser.id,
        );
        setPendingRequests(receivedRequests);

        setSelectedUsers(connectedUsers);
      } catch (error) {
        console.error("Error fetching data:", error);
      }
    };

    fetchRelations();
  }, [currentUser.id, users]);

  useEffect(() => {
    console.log("Pending requests:", pendingRequests);
    console.log("Selected users:", selectedUsers);
    console.log("Available users:", users);
  }, [pendingRequests, selectedUsers, users]);

  const hasPendingRequest = (userId: string) => {
    return pendingRequests.some(
      (request) =>
        (request.userId === currentUser.id &&
          request.relatedUserId === userId) ||
        (request.userId === userId && request.relatedUserId === currentUser.id),
    );
  };

  const isUserDisabled = (userId: string) => {
    // User is current user
    if (userId === currentUser.id) return true;

    // Check existing relationships
    const hasRelation = selectedUsers.some(
      (u: UserRepresentation) => u.id === userId,
    );

    // Check pending requests in both directions
    const hasPending = pendingRequests.some(
      (req) =>
        (req.userId === currentUser.id && req.relatedUserId === userId) ||
        (req.userId === userId && req.relatedUserId === currentUser.id) ||
        (req.userId === currentUser.id && req.relatedUserId === userId),
    );

    return hasRelation || hasPending;
  };

  const refresh = async () => {
    try {
      const [relations, requests] = await Promise.all([
        relationshipService.getRelations(currentUser.id!),
        friendsRequestsService.getFriendsRequests(currentUser.id!),
      ]);

      // Filter out users who are already connected
      const connectedUsers = users.filter((user) =>
        relations.some(
          (relation) =>
            (relation.userId === user.id ||
              relation.relatedUserId === user.id) &&
            relation.relationshipStatus === "ACCEPTED",
        ),
      );

      setSelectedUsers(connectedUsers);
      // Only show received requests in the notification badge
      const receivedRequests = requests!.filter(
        (request) => request.relatedUserId === currentUser.id,
      );
      setPendingRequests(receivedRequests);
    } catch (error) {
      addError("refresh", error);
    }
  };

  const handleAcceptRequest = async (userId: string, relatedUserId: string) => {
    try {
      await friendsRequestsService.acceptFriendRequest(userId, relatedUserId);
      // await relationshipService.updateRelationship(userId, relatedUserId, "ACCEPTED", "FRIENDS");
      await refresh(); // Refresh both lists
      addAlert(t("friendRequestAccepted"), AlertVariant.success);
      document.dispatchEvent(new CustomEvent("friendsRequestsUpdated"));
    } catch (error) {
      addError("acceptRequest", error);
    }
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
        relatedUserId: targetUserId,
      });

      // Update both states
      setSentRequests((prev: any) => new Set([...prev, targetUserId]));

      addAlert(t("friendRequestSent"), AlertVariant.success);
    } catch (error: any) {
      addError(error.messageKey, error.message);
    }
  };

  return (
    <>
      {pendingRequests && pendingRequests.length > 0 && (
        <PageSection variant="light">
          <Popover
            bodyContent={
              <DataList aria-label="Friend requests">
                {pendingRequests.map((request) => (
                  <DataListItem key={request.userId}>
                    <DataListItemRow>
                      <DataListItemCells
                        dataListCells={[
                          <DataListCell key="name">
                            {
                              users.find((u) => u.id === request.userId)
                                ?.username
                            }
                          </DataListCell>,
                          <DataListCell key="actions">
                            <Button
                              variant="primary"
                              onClick={() =>
                                handleAcceptRequest(
                                  request.userId,
                                  request.relatedUserId,
                                )
                              }
                            >
                              Accept
                            </Button>
                            <Button
                              variant="danger"
                              onClick={() =>
                                handleDeclineRequest(
                                  request.userId,
                                  request.relatedUserId,
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
            }
            triggerAction={"hover"}
          >
            <div style={{ display: "inline-block", position: "relative" }}>
              <Alert
                variant="info"
                isInline
                title={t("newFriendRequests 🚀")}
                className="notification-alert"
                style={{ width: "auto", backgroundColor: "yellow" }}
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
      )}
      <PageSection>
        <DataList aria-label="Available users">
          {users.map((user) => (
            <DataListItem key={`select-${user.id}`}>
              <DataListItemRow>
                <DataListCheck
                  aria-labelledby={`select-${user.id}`}
                  name={`select-${user.id}`}
                  isChecked={
                    isUserDisabled(user.id!) ||
                    checkedUsers.has(user) ||
                    sentRequests.has(user.id!) ||
                    hasPendingRequest(user.id!)
                  }
                  isDisabled={true}
                />
                <DataListCell>{user.username}</DataListCell>
                <DataListCell>
                  <Button
                    variant="secondary"
                    isDisabled={
                      hasPendingRequest(user.id!) ||
                      selectedUsers?.some((u) => u.id === user.id) ||
                      sentRequests.has(user.id!)
                    }
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
