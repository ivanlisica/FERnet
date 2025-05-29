import { Component, OnInit, OnDestroy, ViewChild, ElementRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ChatService } from '../../services/chat.service';
import { WebSocketService } from '../../services/websocket.service';
import { AuthService } from '../../services/auth.service';
import { ChatMessageDto, ConversationDto } from '../../models/chat.models';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-admin-chat',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-chat.component.html',
  styleUrls: ['./admin-chat.component.scss']
})
export class AdminChatComponent implements OnInit, OnDestroy {
  @ViewChild('messagesContainer') messagesContainer!: ElementRef;

  private chatService = inject(ChatService);
  private webSocketService = inject(WebSocketService);
  public authService = inject(AuthService);

  conversations: ConversationDto[] = [];
  selectedConversation: ConversationDto | null = null;
  messages: ChatMessageDto[] = [];
  newMessage: string = '';
  currentUser: any;

  private messageSubscription: Subscription | null = null;

  ngOnInit(): void {
    this.currentUser = this.authService.getCurrentUser();
    this.loadConversations();
    this.connectWebSocket();
  }

  ngOnDestroy(): void {
    if (this.messageSubscription) {
      this.messageSubscription.unsubscribe();
    }
    this.webSocketService.disconnect();
  }

  loadConversations(): void {
    this.chatService.getAdminConversations().subscribe({
      next: (conversations) => {
        this.conversations = conversations;
      },
      error: (error) => {
        console.error('Greška pri dohvaćanju konverzacija:', error);
      }
    });
  }

  private connectWebSocket(): void {
    this.webSocketService.connect();
    this.messageSubscription = this.webSocketService.messages$.subscribe({
      next: (message) => {
        if (message && this.selectedConversation &&
            message.conversationId === this.selectedConversation.id) {
          this.messages.push(message);
          setTimeout(() => this.scrollToBottom(), 100);
        }
        this.loadConversations();
      }
    });
  }

  selectConversation(conversation: ConversationDto): void {
    this.selectedConversation = conversation;
    this.loadMessages();
  }

  claimConversation(conversation: ConversationDto): void {
    this.chatService.claimConversation(conversation.id).subscribe({
      next: (updatedConversation) => {
        const index = this.conversations.findIndex(c => c.id === conversation.id);
        if (index !== -1) {
          this.conversations[index] = updatedConversation;
        }
        this.selectedConversation = updatedConversation;
        this.loadMessages();
      },
      error: (error) => {
        console.error('Greška pri preuzimanju konverzacije:', error);
      }
    });
  }

  private loadMessages(): void {
    if (this.selectedConversation) {
      this.chatService.getConversationMessages(this.selectedConversation.id).subscribe({
        next: (messages) => {
          this.messages = messages;
          setTimeout(() => this.scrollToBottom(), 100);
        },
        error: (error) => {
          console.error('Greška pri dohvaćanju poruka:', error);
        }
      });
    }
  }

  sendMessage(): void {
    if (!this.newMessage.trim() || !this.selectedConversation) return;

    const message: ChatMessageDto = {
      content: this.newMessage,
      conversationId: this.selectedConversation.id
    };

    this.webSocketService.sendMessage(message);
    this.newMessage = '';
  }

  private scrollToBottom(): void {
    if (this.messagesContainer) {
      const element = this.messagesContainer.nativeElement;
      element.scrollTop = element.scrollHeight;
    }
  }

  onKeyPress(event: KeyboardEvent): void {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.sendMessage();
    }
  }

  formatDate(date: Date | string): string {
    return new Date(date).toLocaleString('hr-HR');
  }
}
