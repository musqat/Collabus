import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

class WebSocketClient {
  constructor() {
    this.client = null;
    this.connected = false;
    this.subscribers = new Map();
  }

  connect(userId, onMessageCallback) {
    if (this.connected) {
      console.log('WebSocket already connected');
      return;
    }

    this.client = new Client({
      webSocketFactory: () => new SockJS('http://localhost/ws'),
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,

      onConnect: () => {
        console.log('WebSocket connected');
        this.connected = true;

        // 개인 알림 구독
        const subscription = this.client.subscribe(
          `/user/${userId}/queue/notifications`,
          (message) => {
            const notification = JSON.parse(message.body);
            console.log('Received notification:', notification);
            if (onMessageCallback) {
              onMessageCallback(notification);
            }
          }
        );

        this.subscribers.set('notifications', subscription);
      },

      onStompError: (frame) => {
        console.error('WebSocket error:', frame);
        this.connected = false;
      },

      onWebSocketClose: () => {
        console.log('WebSocket closed');
        this.connected = false;
      },

      onDisconnect: () => {
        console.log('WebSocket disconnected');
        this.connected = false;
      }
    });

    this.client.activate();
  }

  disconnect() {
    if (this.client) {
      this.subscribers.forEach(sub => sub.unsubscribe());
      this.subscribers.clear();
      this.client.deactivate();
      this.connected = false;
      console.log('WebSocket disconnected');
    }
  }

  isConnected() {
    return this.connected;
  }
}

export const webSocketClient = new WebSocketClient();
