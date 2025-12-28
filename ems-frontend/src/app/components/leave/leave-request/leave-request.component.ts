import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatSnackBar } from '@angular/material/snack-bar';
import { LeaveService } from '../../../services/leave.service';
import { EmployeeService } from '../../../services/employee.service';
import { Employee } from '../../../models/employee.model';

@Component({
  selector: 'app-leave-request',
  templateUrl: './leave-request.component.html',
  styleUrls: ['./leave-request.component.css'],
})
export class LeaveRequestComponent implements OnInit {
  leaveForm: FormGroup;
  loading = false;
  leaveBalance: any;
  employee?: Employee;
  minDate = new Date();
  maxDate = new Date(new Date().setFullYear(new Date().getFullYear() + 1));

  leaveTypes = [
    { value: 'VACATION', label: 'Vacation', icon: 'beach_access' },
    { value: 'SICK', label: 'Sick Leave', icon: 'medical_services' },
    { value: 'PERSONAL', label: 'Personal', icon: 'person' },
    { value: 'MATERNITY', label: 'Maternity', icon: 'family_restroom' },
    { value: 'PATERNITY', label: 'Paternity', icon: 'family_restroom' },
    { value: 'UNPAID', label: 'Unpaid Leave', icon: 'money_off' },
  ];

  constructor(
    private fb: FormBuilder,
    private leaveService: LeaveService,
    private employeeService: EmployeeService,
    private snackBar: MatSnackBar
  ) {
    this.leaveForm = this.createForm();
  }

  ngOnInit(): void {
    this.loadLeaveBalance();
    this.loadEmployeeData();
  }

  createForm(): FormGroup {
    return this.fb.group({
      leaveType: ['VACATION', Validators.required],
      startDate: ['', Validators.required],
      endDate: ['', Validators.required],
      reason: ['', [Validators.required, Validators.minLength(10)]],
      contactDuringLeave: [''],
      handoverTo: [''],
      isHalfDay: [false],
      halfDayType: ['FIRST_HALF'],
    });
  }

  loadLeaveBalance(): void {
    this.leaveService.getLeaveBalance().subscribe((balance) => {
      this.leaveBalance = balance;
    });
  }

  loadEmployeeData(): void {
    // Get current employee data
    this.employeeService.getEmployeeById(1).subscribe((employee) => {
      this.employee = employee;
    });
  }

  calculateDays(): number {
    const start = this.leaveForm.get('startDate')?.value;
    const end = this.leaveForm.get('endDate')?.value;
    const isHalfDay = this.leaveForm.get('isHalfDay')?.value;

    if (!start || !end) return 0;

    const startDate = new Date(start);
    const endDate = new Date(end);
    const diffTime = Math.abs(endDate.getTime() - startDate.getTime());
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24)) + 1;

    return isHalfDay ? diffDays * 0.5 : diffDays;
  }

  getAvailableDays(): number {
    const leaveType = this.leaveForm.get('leaveType')?.value;
    if (!this.leaveBalance || !leaveType) return 0;

    switch (leaveType) {
      case 'VACATION':
        return this.leaveBalance.vacationDays - this.leaveBalance.usedVacation;
      case 'SICK':
        return this.leaveBalance.sickDays - this.leaveBalance.usedSick;
      case 'PERSONAL':
        return this.leaveBalance.personalDays - this.leaveBalance.usedPersonal;
      default:
        return 999; // Unlimited for other types
    }
  }

  onSubmit(): void {
    if (this.leaveForm.invalid) {
      this.markFormGroupTouched(this.leaveForm);
      return;
    }

    const requestedDays = this.calculateDays();
    const availableDays = this.getAvailableDays();

    if (requestedDays > availableDays) {
      this.snackBar.open(
        `Insufficient leave balance. Available: ${availableDays} days`,
        'Close',
        { duration: 5000 }
      );
      return;
    }

    this.loading = true;
    const formValue = this.leaveForm.value;

    const leaveRequest = {
      leaveType: formValue.leaveType,
      startDate: new Date(formValue.startDate).toISOString().split('T')[0],
      endDate: new Date(formValue.endDate).toISOString().split('T')[0],
      reason: formValue.reason,
      totalDays: requestedDays,
      additionalInfo: {
        contactDuringLeave: formValue.contactDuringLeave,
        handoverTo: formValue.handoverTo,
        isHalfDay: formValue.isHalfDay,
        halfDayType: formValue.halfDayType,
      },
    };

    this.leaveService.requestLeave(leaveRequest).subscribe({
      next: () => {
        this.snackBar.open('Leave request submitted successfully!', 'Close', {
          duration: 3000,
        });
        this.leaveForm.reset({
          leaveType: 'VACATION',
          isHalfDay: false,
          halfDayType: 'FIRST_HALF',
        });
        this.loadLeaveBalance();
        this.loading = false;
      },
      error: (error) => {
        this.snackBar.open('Failed to submit leave request', 'Close', {
          duration: 5000,
        });
        this.loading = false;
      },
    });
  }

  private markFormGroupTouched(formGroup: FormGroup): void {
    Object.values(formGroup.controls).forEach((control) => {
      control.markAsTouched();
      if (control instanceof FormGroup) {
        this.markFormGroupTouched(control);
      }
    });
  }
}
