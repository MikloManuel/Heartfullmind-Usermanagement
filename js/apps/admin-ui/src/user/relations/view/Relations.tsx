import {
  DataList,
  DataListCell,
  DataListCheck,
  DataListItem,
  DataListItemRow,
} from "@patternfly/react-core";
import { RelationshipRepresentation } from "../data/RelationshipRepresentation";
import UserRepresentation from "js/libs/keycloak-admin-client/lib/defs/userRepresentation";
import { useEffect, useState } from "react";
import { RelationshipService } from "../RelationshipService";
import { useRealm } from "../../../context/realm-context/RealmContext";
import type RealmRepresentation from "@keycloak/keycloak-admin-client/lib/defs/realmRepresentation";

const RelationshipTab = ({
  user,
  users,
}: {
  user: UserRepresentation;
  users: UserRepresentation[];
}) => {
  const { realm } = useRealm() as { realm: RealmRepresentation };
  const serverUrl = realm?.attributes?.["serverUrl"] || window.location.origin;
  const relationshipService = new RelationshipService(serverUrl);
  const [selectedUsers, setSelectedUsers] = useState<UserRepresentation[]>();

  function refresh() {
    relationshipService.getRelations(user.id!).then((relations) => {
      console.log("Relations received:", relations);
      const matchingUsers = users.filter((u) =>
        relations!.some(
          (relation) =>
            relation.relatedUserId === u.id || relation.userId === u.id,
        ),
      );
      console.log("Setting matching users:", matchingUsers);
      setSelectedUsers(matchingUsers);
    });
  }

  useEffect(() => {
    const handleRelationsUpdate = () => {
      // Refresh relations data
      refresh();
    };

    // Add event listener
    document.addEventListener("relationsUpdated", handleRelationsUpdate);

    // Cleanup
    return () => {
      document.removeEventListener("relationsUpdated", handleRelationsUpdate);
    };
  }, []);

  useEffect(() => {
    console.log("Effect running with:", {
      userId: user.id,
      usersLength: users.length,
      currentTime: new Date().toISOString(),
    });
    refresh();
  }, [user.id, users]);

  const handleSelectionChange = (userId: string, checked: boolean) => {
    if (checked) {
      const rel = {
        userId: user.id,
        relatedUserId: userId,
        relationshipType: "NONE",
        relationshipStatus: "PENDING",
      } as RelationshipRepresentation;

      relationshipService.addRelationship(rel).then(() => {
        setSelectedUsers((prev) => [
          ...prev!,
          users.find((u) => u.id === userId)!,
        ]);
      });
    } else {
      relationshipService.deleteRelationship(user.id!, userId).then(() => {
        setSelectedUsers((prev) => prev!.filter((u) => u.id !== userId));
      });
    }
  };

  return (
    <DataList aria-label="Selectable user list">
      {users.map((user) => (
        <DataListItem key={user.id} aria-labelledby={`select-${user.id}`}>
          <DataListItemRow>
            <DataListCheck
              aria-labelledby={`select-${user.id}`}
              name={`select-${user.id}`}
              isChecked={selectedUsers?.some((u) => u.id === user.id) || false}
              onChange={(
                _event: React.FormEvent<HTMLInputElement>,
                checked: boolean,
              ) => handleSelectionChange(user.id!, checked)}
            />
            <DataListCell>{user.username}</DataListCell>
          </DataListItemRow>
        </DataListItem>
      ))}
    </DataList>
  );
};

export default RelationshipTab;
