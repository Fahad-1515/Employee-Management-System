import { Injectable, signal } from '@angular/core';
import { BehaviorSubject, Observable, interval } from 'rxjs';
import { map, shareReplay } from 'rxjs/operators';

@Injectable({
  providedIn: 'root',
})
export class RealTimeService {
  private notifications = signal<any[]>([]);
  private liveStats = signal({
    totalEmployees: 0,
    activeEmployees: 0,
    pendingLeaves: 0,
    recentActivities: 0,
  });

  notifications$ = this.notifications.asReadonly();
  liveStats$ = this.liveStats.asReadonly();

  // Simulate real-time updates
  private updateInterval = interval(10000); // Update every 10 seconds

  constructor() {
    this.initializeRealTimeUpdates();
  }

  private initializeRealTimeUpdates(): void {
    // Simulate receiving notifications
    this.updateInterval.subscribe(() => {
      this.updateNotifications();
      this.updateLiveStats();
    });

    // Initial data
    this.loadInitialData();
  }

  private loadInitialData(): void {
    this.notifications.set([
      {
        id: 1,
        type: 'EMPLOYEE_ADDED',
        message: 'New employee John Doe added',
        timestamp: new Date(Date.now() - 300000), // 5 minutes ago
        read: false,
      },
      {
        id: 2,
        type: 'LEAVE_REQUEST',
        message: 'Jane Smith requested leave',
        timestamp: new Date(Date.now() - 600000), // 10 minutes ago
        read: true,
      },
      {
        id: 3,
        type: 'SALARY_REVIEW',
        message: 'Salary review due for Mike Johnson',
        timestamp: new Date(Date.now() - 900000), // 15 minutes ago
        read: false,
      },
    ]);

    this.liveStats.set({
      totalEmployees: 45,
      activeEmployees: 38,
      pendingLeaves: 3,
      recentActivities: 12,
    });
  }

  private updateNotifications(): void {
    const newNotification = {
      id: Date.now(),
      type: 'SYSTEM_UPDATE',
      message: `System update at ${new Date().toLocaleTimeString()}`,
      timestamp: new Date(),
      read: false,
    };

    this.notifications.update((notifications) => [
      newNotification,
      ...notifications.slice(0, 9), // Keep only 10 latest
    ]);
  }

  private updateLiveStats(): void {
    // Simulate changing stats
    const current = this.liveStats();
    this.liveStats.set({
      totalEmployees:
        current.totalEmployees + Math.floor(Math.random() * 3) - 1,
      activeEmployees: current.activeEmployees + Math.floor(Math.random() * 2),
      pendingLeaves: current.pendingLeaves + Math.floor(Math.random() * 2) - 1,
      recentActivities: current.recentActivities + 1,
    });
  }

  markAsRead(notificationId: number): void {
    this.notifications.update((notifications) =>
      notifications.map((notification) =>
        notification.id === notificationId
          ? { ...notification, read: true }
          : notification
      )
    );
  }

  markAllAsRead(): void {
    this.notifications.update((notifications) =>
      notifications.map((notification) => ({ ...notification, read: true }))
    );
  }

  clearNotifications(): void {
    this.notifications.set([]);
  }

  getUnreadCount(): number {
    return this.notifications().filter((n) => !n.read).length;
  }

  // Simulate WebSocket connection
  connect(): void {
    console.log('🔌 Real-time service connected');
  }

  disconnect(): void {
    console.log('🔌 Real-time service disconnected');
  }
}
