export interface ChatMessageDto {
  senderId?: string;
  senderUsername?: string;
  receiverId?: string;
  conversationId?: number;
  content: string;
  timestamp?: Date;
  isRead?: boolean;
  messageId?: number;
}

export interface ConversationDto {
  id: number;
  clientUsername: string;
  adminUsername?: string;
  lastMessageAt: Date;
  createdAt: Date;
  isClosed: boolean;
}

export interface UserDto {
  username: string;
  firstName: string;
  lastName: string;
}
