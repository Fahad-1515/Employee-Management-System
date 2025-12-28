import { Component, OnInit, ViewChild } from '@angular/core';
import { MatTableDataSource } from '@angular/material/table';
import { MatPaginator, PageEvent } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { LeaveService } from '../../../services/leave.service';
import { LeaveRequest } from '../../../models/leave.model';
import { LeaveApprovalDialogComponent } from '../leave-approval-dialog/leave-approval-dialog.component';

@Component({
  selector: 'app-leave-management',
  templateUrl: './leave-management.component.html',
  styleUrls: ['./leave-management.component.css'],
})
export class LeaveManagementComponent implements OnInit {
  displayedColumns: string[] = [
    'employee',
    'leaveType',
    'dates',
    'duration',
    'reason',
    'status',
    'actions',
  ];
  dataSource = new MatTableDataSource<LeaveRequest>();
  loading = false;

  // Filters
  statusFilter = '';
  dateRange = { start: '', end: '' };

  // Stats
  stats = {
    pending: 0,
    approved: 0,
    rejected: 0,
    total: 0,
  };

  // Pagination
  totalElements = 0;
  pageSize = 10;
  currentPage = 0;

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  statusOptions = [
    { value: '', label: 'All Status' },
    { value: 'PENDING', label: 'Pending' },
    { value: 'APPROVED', label: 'Approved' },
    { value: 'REJECTED', label: 'Rejected' },
  ];

  constructor(
    private leaveService: LeaveService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.loadLeaveRequests();
    this.loadStats();
  }

  ngAfterViewInit(): void {
    this.dataSource.paginator = this.paginator;
    this.dataSource.sort = this.sort;
  }

  loadLeaveRequests(): void {
    this.loading = true;
    this.leaveService
      .getLeaveRequests(this.currentPage, this.pageSize, this.statusFilter)
      .subscribe({
        next: (response) => {
          this.dataSource.data = response.content;
          this.totalElements = response.totalElements;
          this.loading = false;
        },
        error: (error) => {
          console.error('Error loading leave requests:', error);
          this.loading = false;
          this.snackBar.open('Error loading leave requests', 'Close', {
            duration: 5000,
          });
        },
      });
  }

  loadStats(): void {
    this.leaveService.getLeaveStats().subscribe({
      next: (stats) => {
        this.stats = {
          pending: stats.pendingRequests || 0,
          approved: stats.approvedThisMonth || 0,
          rejected: stats.rejectedThisMonth || 0,
          total: stats.totalLeavesTaken || 0,
        };
      },
      error: (error) => {
        console.error('Error loading stats:', error);
        // Set default stats if error
        this.stats = {
          pending: 0,
          approved: 0,
          rejected: 0,
          total: 0,
        };
      },
    });
  }

  onPageChange(event: PageEvent): void {
    this.currentPage = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadLeaveRequests();
  }

  applyFilters(): void {
    this.currentPage = 0;
    this.loadLeaveRequests();
  }

  clearFilters(): void {
    this.statusFilter = '';
    this.dateRange = { start: '', end: '' };
    this.applyFilters();
  }

  openApprovalDialog(leave: LeaveRequest): void {
    const dialogRef = this.dialog.open(LeaveApprovalDialogComponent, {
      width: '500px',
      data: { leave },
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.loadLeaveRequests();
        this.loadStats();
      }
    });
  }

  getStatusColor(status: string): string {
    switch (status) {
      case 'PENDING':
        return 'warning';
      case 'APPROVED':
        return 'success';
      case 'REJECTED':
        return 'error';
      default:
        return 'default';
    }
  }

  getStatusIcon(status: string): string {
    switch (status) {
      case 'PENDING':
        return 'schedule';
      case 'APPROVED':
        return 'check_circle';
      case 'REJECTED':
        return 'cancel';
      default:
        return 'help';
    }
  }

  getLeaveTypeIcon(type: string): string {
    switch (type) {
      case 'VACATION':
        return 'beach_access';
      case 'SICK':
        return 'medical_services';
      case 'PERSONAL':
        return 'person';
      case 'MATERNITY':
        return 'family_restroom';
      case 'PATERNITY':
        return 'family_restroom';
      default:
        return 'event';
    }
  }

  formatDate(dateString: string): string {
    if (!dateString) return '';
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
    });
  }

  getDateRange(startDate: string, endDate: string): string {
    return `${this.formatDate(startDate)} - ${this.formatDate(endDate)}`;
  }

  refreshData(): void {
    this.loadLeaveRequests();
    this.loadStats();
  }
}
