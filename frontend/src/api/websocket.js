import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

const WS_URL = import.meta.env.VITE_WS_BASE_URL;

class WebSocketClient {
  constructor() {
    this.client = null;
    this.connected = false;
    this.subscribers = new Map();
  }

  connect(userId, onMessageCallback) {
    if (this.connected) return;

    const token = localStorage.getItem('accessToken');

    this.client = new Client({
      webSocketFactory: () => new SockJS(WS_URL),
      connectHeaders: {
        Authorization: `Bearer ${token}`,
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,

      onConnect: () => {
        this.connected = true;
        const subscription = this.client.subscribe(
          `/user/${userId}/queue/notifications`,
          (message) => {
            const notification = JSON.parse(message.body);
            if (onMessageCallback) onMessageCallback(notification);
          }
        );
        this.subscribers.set('notifications', subscription);
      },

      onStompError: (frame) => {
        console.error('WebSocket STOMP error:', frame);
        this.connected = false;
      },

      onWebSocketClose: () => {
        this.connected = false;
      },

      onDisconnect: () => {
        this.connected = false;
      },
    });

    this.client.activate();
  }

  disconnect() {
    if (this.client) {
      this.subscribers.forEach(sub => sub.unsubscribe());
      this.subscribers.clear();
      this.client.deactivate();
      this.connected = false;
    }
  }

  isConnected() {
    return this.connected;
  }
}

export const webSocketClient = new WebSocketClient();
