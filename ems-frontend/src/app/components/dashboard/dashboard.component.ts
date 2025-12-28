import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { MatDialog } from '@angular/material/dialog'; // Add this import
import { AuthService } from '../../services/auth.service';
import { EmployeeService } from '../../services/employee.service';
import { ImportDialogComponent } from '../shared/import-dialog/import-dialog.component';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css'],
})
export class DashboardComponent implements OnInit {
  user: any;
  loading = false;

  stats = {
    totalEmployees: 0,
    totalDepartments: 0,
    avgSalary: 0,
    recentHires: 0,
    totalSalary: 0,
    activeProjects: 8,
  };

  quickActions = [
    {
      icon: 'person_add',
      title: 'Add Employee',
      description: 'Create new employee record',
      color: 'primary',
      route: '/employees',
      action: 'add',
    },
    {
      icon: 'search',
      title: 'Search Employees',
      description: 'Find employees quickly',
      color: 'accent',
      route: '/employees',
      action: 'search',
    },
    {
      icon: 'assessment',
      title: 'Reports',
      description: 'Generate detailed reports',
      color: 'warn',
      route: '/analytics',
      action: 'reports',
    },
    {
      icon: 'download',
      title: 'Export Data',
      description: 'Export to Excel/CSV',
      color: 'primary',
      route: '/employees',
      action: 'export',
    },
    {
      icon: 'upload',
      title: 'Import Data',
      description: 'Bulk upload from CSV/Excel',
      color: 'accent',
      action: 'import',
    },
  ];

  recentActivities = [
    {
      action: 'New employee added',
      user: 'John Doe',
      time: '2 hours ago',
      icon: 'person_add',
    },
    {
      action: 'Profile updated',
      user: 'Sarah Wilson',
      time: '4 hours ago',
      icon: 'edit',
    },
    {
      action: 'Salary revised',
      user: 'Mike Johnson',
      time: '1 day ago',
      icon: 'attach_money',
    },
    {
      action: 'Department changed',
      user: 'Lisa Taylor',
      time: '2 days ago',
      icon: 'business',
    },
  ];

  constructor(
    public authService: AuthService,
    private employeeService: EmployeeService,
    private router: Router,
    private dialog: MatDialog // Add dialog to constructor
  ) {}

  ngOnInit(): void {
    this.authService.currentUser$.subscribe((user) => {
      this.user = user;
    });
    this.loadDashboardStats();
  }

  loadDashboardStats(): void {
    this.loading = true;
    this.employeeService.searchEmployees(0, 1000).subscribe({
      next: (response) => {
        const employees = response.content;
        this.stats.totalEmployees = response.totalElements;
        this.stats.totalSalary = employees.reduce(
          (sum: number, emp: any) => sum + (emp.salary || 0),
          0
        );
        this.stats.avgSalary =
          employees.length > 0 ? this.stats.totalSalary / employees.length : 0;

        // Calculate unique departments
        const departments = new Set(
          employees.map((emp: any) => emp.department)
        );
        this.stats.totalDepartments = departments.size;

        // Mock recent hires (last 7 days)
        this.stats.recentHires = Math.floor(Math.random() * 5) + 2;

        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading stats:', error);
        this.loading = false;
      },
    });
  }

  openImportDialog(): void {
    const dialogRef = this.dialog.open(ImportDialogComponent, {
      width: '850px',
      maxHeight: '90vh',
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result?.success) {
        this.loadDashboardStats(); // Refresh dashboard stats
      }
    });
  }

  navigateToEmployees(): void {
    this.router.navigate(['/employees']);
  }

  navigateToAnalytics(): void {
    this.router.navigate(['/analytics']);
  }

  performQuickAction(action: string): void {
    switch (action) {
      case 'add':
        this.router.navigate(['/employees']);
        break;
      case 'search':
        this.router.navigate(['/employees'], { queryParams: { search: true } });
        break;
      case 'reports':
        this.router.navigate(['/analytics']);
        break;
      case 'export':
        this.employeeService.exportToExcel().subscribe();
        break;
      case 'import':
        this.openImportDialog();
        break;
    }
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  getGreeting(): string {
    const hour = new Date().getHours();
    if (hour < 12) return 'Good Morning';
    if (hour < 18) return 'Good Afternoon';
    return 'Good Evening';
  }
}
