import { Injectable, inject } from '@angular/core';
import { Client, IMessage } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { BehaviorSubject } from 'rxjs';
import { ChatMessageDto } from '../models/chat.models';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class WebSocketService {
  private authService = inject(AuthService);
  private stompClient: Client | null = null;
  private messagesSubject = new BehaviorSubject<ChatMessageDto | null>(null);
  public messages$ = this.messagesSubject.asObservable();
  private connected = false;

  connect(): void {
    if (this.connected) return;

    const token = this.authService.getToken();
    if (!token) return;

    this.stompClient = new Client({
      webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
      connectHeaders: {
        Authorization: `Bearer ${token}`
      },
      debug: (str) => console.log('STOMP: ' + str),
      onConnect: () => {
        console.log('Connected to WebSocket');
        this.connected = true;
        this.subscribeToPrivateMessages();
      },
      onDisconnect: () => {
        console.log('Disconnected from WebSocket');
        this.connected = false;
      },
      onStompError: (frame) => {
        console.error('STOMP error:', frame);
      }
    });

    this.stompClient.activate();
  }

  private subscribeToPrivateMessages(): void {
    if (this.stompClient && this.stompClient.connected) {
      this.stompClient.subscribe('/user/queue/private', (message: IMessage) => {
        const chatMessage: ChatMessageDto = JSON.parse(message.body);
        this.messagesSubject.next(chatMessage);
      });
    }
  }

  sendMessage(message: ChatMessageDto): void {
    if (this.stompClient && this.stompClient.connected) {
      this.stompClient.publish({
        destination: '/app/chat.sendMessage',
        body: JSON.stringify(message)
      });
    }
  }

  disconnect(): void {
    if (this.stompClient) {
      this.stompClient.deactivate();
      this.connected = false;
    }
  }

  isConnected(): boolean {
    return this.connected;
  }
}
