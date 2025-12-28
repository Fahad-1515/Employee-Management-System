import { Component, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { LeaveService } from '../../../services/leave.service';

@Component({
  selector: 'app-leave-approval-dialog',
  templateUrl: './leave-approval-dialog.component.html',
  styleUrls: ['./leave-approval-dialog.component.css'],
})
export class LeaveApprovalDialogComponent {
  comments = '';
  loading = false;

  constructor(
    public dialogRef: MatDialogRef<LeaveApprovalDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    private leaveService: LeaveService,
    private snackBar: MatSnackBar
  ) {}

  approve(): void {
    this.updateStatus('APPROVED');
  }

  reject(): void {
    this.updateStatus('REJECTED');
  }

  private updateStatus(status: string): void {
    if (!this.data?.leave?.id) {
      this.snackBar.open('Invalid leave request', 'Close', { duration: 3000 });
      return;
    }

    this.loading = true;
    this.leaveService
      .updateLeaveStatus(this.data.leave.id, status, this.comments)
      .subscribe({
        next: () => {
          this.snackBar.open(
            `Leave request ${status.toLowerCase()} successfully`,
            'Close',
            {
              duration: 3000,
            }
          );
          this.loading = false;
          this.dialogRef.close({ success: true, status });
        },
        error: (error) => {
          console.error('Error updating leave status:', error);
          this.snackBar.open('Failed to update leave status', 'Close', {
            duration: 5000,
          });
          this.loading = false;
        },
      });
  }

  cancel(): void {
    this.dialogRef.close({ success: false });
  }
}
