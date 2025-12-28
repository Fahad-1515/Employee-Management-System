import { Component, OnInit } from '@angular/core';
import { Router, NavigationEnd } from '@angular/router';
import { AuthService } from './services/auth.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css'],
})
export class AppComponent implements OnInit {
  title = 'Employee Management System';
  currentUser: any = null;
  isSidebarOpen = true;
  userRole = 'User';
  userInitials = 'U';
  showHeader = false;

  // Navigation items
  navItems = [
    {
      icon: 'dashboard',
      label: 'Dashboard',
      route: '/dashboard',
      roles: ['ADMIN', 'USER'],
    },
    {
      icon: 'people',
      label: 'Employees',
      route: '/employees',
      roles: ['ADMIN', 'USER'],
    },
    {
      icon: 'analytics',
      label: 'Analytics',
      route: '/analytics',
      roles: ['ADMIN', 'USER'],
    },
    {
      icon: 'beach_access',
      label: 'Leave Request',
      route: '/leave/request',
      roles: ['ADMIN', 'USER'],
    },
    {
      icon: 'assignment',
      label: 'Leave Management',
      route: '/leave/manage',
      roles: ['ADMIN'],
    },
    {
      icon: 'calendar_today',
      label: 'Leave Calendar',
      route: '/leave/calendar',
      roles: ['ADMIN', 'USER'],
    },
    {
      icon: 'person',
      label: 'My Profile',
      route: '/profile',
      roles: ['ADMIN', 'USER'],
    },
  ];

  constructor(public authService: AuthService, private router: Router) {}

  ngOnInit(): void {
    // Subscribe to user changes
    this.authService.currentUser$.subscribe((user) => {
      this.currentUser = user;
      if (user) {
        this.userRole = user.role || 'USER';
        this.userInitials = user.username?.charAt(0).toUpperCase() || 'U';
        this.showHeader = true;
      } else {
        this.showHeader = false;
      }
    });

    // Hide sidebar on login page
    this.router.events.subscribe((event) => {
      if (event instanceof NavigationEnd) {
        this.showHeader = !event.url.includes('/login');
      }
    });
  }

  toggleSidebar(): void {
    this.isSidebarOpen = !this.isSidebarOpen;
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  navigateTo(route: string): void {
    this.router.navigate([route]);
  }

  // Check if user has permission for a nav item
  hasPermission(roles: string[]): boolean {
    if (!this.currentUser) return false;
    return roles.includes(this.currentUser.role);
  }

  // Get filtered navigation items based on role
  get filteredNavItems(): any[] {
    return this.navItems.filter((item) => this.hasPermission(item.roles));
  }
}
