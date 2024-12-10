import axios from "axios";
import { FriendsRequestsRepresentation } from "./data/FriendsRequestsRepresentation";

export class FriendsRequestsService {
  constructor(private baseUrl: string) {}

  public async getFriendsRequests(userId: string): Promise<FriendsRequestsRepresentation[] | undefined> {
    try {
      console.log('Fetching requests for user:', userId);
      const response = await axios.get(
          `${this.baseUrl}/admin/api/friends-requests/requests/${userId}`
      );
      console.log('Response:', response.data);
      return response.data;
    } catch (error) {
      console.error('Error fetching friends requests:', error);
      throw error;
    }
  }

  public async sendFriendRequest(userDTO: {
    userId: string;
    relatedUserId: string;
  }): Promise<void> {
    await axios.post(
      `${this.baseUrl}/admin/api/friends-requests/request`,
      userDTO,
    );
  }

  public async acceptFriendRequest(
    userId: string,
    relatedUserId: string,
  ): Promise<void> {
    await axios.put(
      `${this.baseUrl}/admin/api/friends-requests/accept/${userId}/${relatedUserId}`,
    );
  }

  public async declineFriendRequest(
    userId: string,
    relatedUserId: string,
  ): Promise<void> {
    await axios.put(
      `${this.baseUrl}/admin/api/friends-requests/decline/${userId}/${relatedUserId}`,
    );
  }
}
