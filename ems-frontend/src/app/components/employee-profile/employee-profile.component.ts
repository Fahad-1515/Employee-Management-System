import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { EmployeeService } from '../../services/employee.service';
import { AuthService } from '../../services/auth.service';
import { Employee } from '../../models/employee.model';
import { LeaveService } from '../../services/leave.service';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { LeaveRequestComponent } from '../leave/leave-request/leave-request.component';

@Component({
  selector: 'app-employee-profile',
  templateUrl: './employee-profile.component.html',
  styleUrls: ['./employee-profile.component.css'],
})
export class EmployeeProfileComponent implements OnInit {
  employee?: Employee;
  leaveBalance: any;
  loading = false;
  isEditing = false;
  isAdmin = false;

  constructor(
    private route: ActivatedRoute,
    private employeeService: EmployeeService,
    private leaveService: LeaveService,
    private authService: AuthService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.isAdmin = this.authService.isAdmin();
    this.loadEmployeeProfile();
  }

  loadEmployeeProfile(): void {
    this.loading = true;
    const employeeId = this.route.snapshot.params['id'] || 1; // Default to ID 1 for demo

    this.employeeService.getEmployeeById(employeeId).subscribe({
      next: (employee) => {
        this.employee = employee;
        this.loadLeaveBalance();
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading employee:', error);
        // Load mock data for demo
        this.loadMockEmployee();
        this.loading = false;
      },
    });
  }

  loadLeaveBalance(): void {
    const employeeId = this.employee?.id || 1;
    this.leaveService.getLeaveBalance(employeeId).subscribe((balance) => {
      this.leaveBalance = balance;
    });
  }

  loadMockEmployee(): void {
    this.employee = {
      id: 1,
      firstName: 'John',
      lastName: 'Doe',
      email: 'john.doe@example.com',
      phoneNumber: '+1234567890',
      countryCode: '+1',
      department: 'Information Technology',
      position: 'Senior Software Engineer',
      salary: 95000,
      hireDate: '2022-01-15',
      profilePicture: '',
      leaveBalance: {
        vacation: 15,
        sick: 10,
        personal: 5,
        usedVacation: 5,
        usedSick: 2,
        usedPersonal: 1,
      },
      emergencyContact: {
        name: 'Jane Doe',
        relationship: 'Spouse',
        phone: '+1234567891',
      },
    };
  }

  getInitials(): string {
    if (!this.employee) return '?';
    return (
      this.employee.firstName[0] + this.employee.lastName[0]
    ).toUpperCase();
  }

  getProfileImage(): string {
    return (
      this.employee?.profilePicture ||
      `https://ui-avatars.com/api/?name=${this.getInitials()}&background=667eea&color=fff&size=200`
    );
  }

  calculateLeavePercentage(type: 'vacation' | 'sick' | 'personal'): number {
    if (!this.leaveBalance) return 0;

    switch (type) {
      case 'vacation':
        return (
          (this.leaveBalance.usedVacation / this.leaveBalance.vacationDays) *
          100
        );
      case 'sick':
        return (this.leaveBalance.usedSick / this.leaveBalance.sickDays) * 100;
      case 'personal':
        return (
          (this.leaveBalance.usedPersonal / this.leaveBalance.personalDays) *
          100
        );
      default:
        return 0;
    }
  }

  openLeaveRequestDialog(): void {
    const dialogRef = this.dialog.open(LeaveRequestComponent, {
      width: '600px',
      maxHeight: '90vh',
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.loadLeaveBalance();
      }
    });
  }

  editProfile(): void {
    this.isEditing = true;
  }

  saveProfile(): void {
    // In real app, call API to update employee
    this.snackBar.open('Profile updated successfully!', 'Close', {
      duration: 3000,
    });
    this.isEditing = false;
  }

  cancelEdit(): void {
    this.isEditing = false;
  }
}
