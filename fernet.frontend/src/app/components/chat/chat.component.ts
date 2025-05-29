import { Component, OnInit, OnDestroy, ViewChild, ElementRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ChatService } from '../../services/chat.service';
import { WebSocketService } from '../../services/websocket.service';
import { AuthService } from '../../services/auth.service';
import { ChatMessageDto, ConversationDto, UserDto } from '../../models/chat.models';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-chat',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './chat.component.html',
  styleUrls: ['./chat.component.scss']
})
export class ChatComponent implements OnInit, OnDestroy {
  @ViewChild('messagesContainer') messagesContainer!: ElementRef;

  private chatService = inject(ChatService);
  private webSocketService = inject(WebSocketService);
  public authService = inject(AuthService);

  messages: ChatMessageDto[] = [];
  newMessage: string = '';
  currentConversation: ConversationDto | null = null;
  availableAdmins: UserDto[] = [];
  selectedAdmin: string | null = null;
  loading = false;
  currentUser: any;

  private messageSubscription: Subscription | null = null;

  ngOnInit(): void {
    this.currentUser = this.authService.getCurrentUser();
    this.loadAdmins();
    this.loadConversation();
    this.connectWebSocket();
  }

  ngOnDestroy(): void {
    if (this.messageSubscription) {
      this.messageSubscription.unsubscribe();
    }
    this.webSocketService.disconnect();
  }

  loadAdmins(): void {
    this.chatService.getAllAdmins().subscribe({
      next: (admins) => {
        this.availableAdmins = admins;
      },
      error: (error) => {
        console.error('Greška pri dohvaćanju administratora:', error);
      }
    });
  }

  private loadConversation(): void {
    this.chatService.getLatestConversation().subscribe({
      next: (conversation) => {
        this.currentConversation = conversation;
        this.loadMessages();
      },
      error: (error) => {
        console.log('Nema aktivne konverzacije');
      }
    });
  }

  private loadMessages(): void {
    if (this.currentConversation) {
      this.chatService.getConversationMessages(this.currentConversation.id).subscribe({
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

  private connectWebSocket(): void {
    this.webSocketService.connect();
    this.messageSubscription = this.webSocketService.messages$.subscribe({
      next: (message) => {
        if (message) {
          this.messages.push(message);
          setTimeout(() => this.scrollToBottom(), 100);
        }
      }
    });
  }

  startConversation(): void {
    if (!this.selectedAdmin) return;

    this.loading = true;
    this.chatService.startConversation(this.selectedAdmin).subscribe({
      next: (conversation) => {
        this.currentConversation = conversation;
        this.messages = [];
        this.loading = false;
      },
      error: (error) => {
        console.error('Greška pri pokretanju konverzacije:', error);
        this.loading = false;
      }
    });
  }

  sendMessage(): void {
    if (!this.newMessage.trim() || !this.currentConversation) return;

    const message: ChatMessageDto = {
      content: this.newMessage,
      conversationId: this.currentConversation.id,
      receiverId: this.currentConversation.adminUsername ? undefined : (this.selectedAdmin ?? undefined)
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
}
