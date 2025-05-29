import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ChatMessageDto, ConversationDto, UserDto } from '../models/chat.models';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class ChatService {
  private http = inject(HttpClient);
  private authService = inject(AuthService);
  private baseUrl = 'http://localhost:8080';

  private getHeaders(): HttpHeaders {
    const token = this.authService.getToken();
    return new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });
  }

  getAllAdmins(): Observable<UserDto[]> {
    return this.http.get<UserDto[]>(`${this.baseUrl}/admin/all`, {
      headers: this.getHeaders()
    });
  }

  startConversation(adminUsername: string): Observable<ConversationDto> {
    return this.http.post<ConversationDto>(`${this.baseUrl}/conversation/start/${adminUsername}`, {}, {
      headers: this.getHeaders()
    });
  }


  getLatestConversation(): Observable<ConversationDto> {
    return this.http.get<ConversationDto>(`${this.baseUrl}/coversation/get/lastest`, {
      headers: this.getHeaders()
    });
  }

  getAdminConversations(): Observable<ConversationDto[]> {
    return this.http.get<ConversationDto[]>(`${this.baseUrl}/conversation/admin-active`, {
      headers: this.getHeaders()
    });
  }

  claimConversation(conversationId: number): Observable<ConversationDto> {
    return this.http.post<ConversationDto>(`${this.baseUrl}/conversation/admin-claim/${conversationId}`, {}, {
      headers: this.getHeaders()
    });
  }

  getConversationMessages(conversationId: number): Observable<ChatMessageDto[]> {
    return this.http.get<ChatMessageDto[]>(`${this.baseUrl}/conversation/messages/${conversationId}`, {
      headers: this.getHeaders()
    });
  }
}
