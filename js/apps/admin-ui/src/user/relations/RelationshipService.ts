import axios from 'axios';
import { RelationshipRepresentation } from "./data/RelationshipRepresentation";

export class RelationshipService {
  constructor(private baseUrl: string) {}

  getRelations(userId: string): Promise<RelationshipRepresentation[]> {
    console.log('Calling backend with userId:', userId);
    return axios.get(`${this.baseUrl}/admin/api/relationships/relations/${userId}`)
        .then((response: any) => {
          console.log('Backend response:', response.data);
          return response.data;
        });
  }


  public async addRelationship(relations: RelationshipRepresentation): Promise<void> {
    // await this.updateRelationship(relations.userId, relations.relatedUserId, relations.relationshipStatus, relations.relationshipType);

    await axios.post(`${this.baseUrl}/admin/api/relationships/relationship`, {
      userId: relations.userId,
      relatedUserId: relations.relatedUserId,
      relationshipStatus: relations.relationshipStatus,
      relationshipType: relations.relationshipType,
    });
  }

  public async updateRelationship(
    userId: string,
    relatedUserId: string,
    status: string,
    type: string,
  ): Promise<void> {
    await axios.put(
      `${this.baseUrl}/admin/api/relationships/update/${userId}/${relatedUserId}/${status}/${type}`,
    );
  }

  public async deleteRelationship(
    userId: string,
    relatedUserId: string,
  ): Promise<void> {
    await axios.delete(
      `${this.baseUrl}/admin/api/relationships/delete/${userId}/${relatedUserId}`,
    );
  }
}
