import { Component, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { EmployeeService } from '../../../services/employee.service';

@Component({
  selector: 'app-export-dialog',
  templateUrl: './export-dialog.component.html',
  styleUrls: ['./export-dialog.component.css'],
})
export class ExportDialogComponent {
  formats = [
    { value: 'excel', label: 'Excel (.xlsx)', icon: 'grid_on' },
    { value: 'csv', label: 'CSV (.csv)', icon: 'table_chart' },
    { value: 'pdf', label: 'PDF (.pdf)', icon: 'picture_as_pdf' },
  ];

  columns = [
    { field: 'id', label: 'Employee ID', selected: true },
    { field: 'firstName', label: 'First Name', selected: true },
    { field: 'lastName', label: 'Last Name', selected: true },
    { field: 'email', label: 'Email', selected: true },
    { field: 'department', label: 'Department', selected: true },
    { field: 'position', label: 'Position', selected: true },
    { field: 'salary', label: 'Salary', selected: true },
    { field: 'phoneNumber', label: 'Phone Number', selected: false },
    { field: 'hireDate', label: 'Hire Date', selected: false },
    { field: 'createdAt', label: 'Created Date', selected: false },
  ];

  selectedFormat = 'excel';
  selectedColumns = this.columns.filter((c) => c.selected).map((c) => c.field);
  includeHeaders = true;
  emailReport = false;
  recipientEmail = '';
  loading = false;

  constructor(
    public dialogRef: MatDialogRef<ExportDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    private employeeService: EmployeeService,
    private snackBar: MatSnackBar
  ) {}

  toggleColumn(column: any): void {
    column.selected = !column.selected;
    this.selectedColumns = this.columns
      .filter((c) => c.selected)
      .map((c) => c.field);
  }

  selectAllColumns(): void {
    this.columns.forEach((c) => (c.selected = true));
    this.selectedColumns = this.columns.map((c) => c.field);
  }

  deselectAllColumns(): void {
    this.columns.forEach((c) => (c.selected = false));
    this.selectedColumns = [];
  }

  exportData(): void {
    if (this.selectedColumns.length === 0) {
      this.snackBar.open('Please select at least one column', 'Close', {
        duration: 3000,
      });
      return;
    }

    this.loading = true;

    // Simulate export
    setTimeout(() => {
      const fileName = `employees_export_${Date.now()}.${this.selectedFormat}`;
      const content = this.generateMockExport();

      if (this.selectedFormat === 'csv') {
        this.downloadCSV(content, fileName);
      } else {
        this.downloadFile(content, fileName);
      }

      if (this.emailReport && this.recipientEmail) {
        this.snackBar.open(`Report sent to ${this.recipientEmail}`, 'Close', {
          duration: 3000,
        });
      }

      this.snackBar.open('Export completed successfully!', 'Close', {
        duration: 3000,
      });

      this.loading = false;
      this.dialogRef.close({ success: true });
    }, 1500);
  }

  generateMockExport(): string {
    // Mock data for demonstration
    const headers = this.columns
      .filter((c) => c.selected)
      .map((c) => c.label)
      .join(',');

    const data = [
      [
        '1',
        'John',
        'Doe',
        'john@example.com',
        'IT',
        'Software Engineer',
        '75000',
      ],
      ['2', 'Jane', 'Smith', 'jane@example.com', 'HR', 'HR Manager', '65000'],
      [
        '3',
        'Mike',
        'Johnson',
        'mike@example.com',
        'Finance',
        'Financial Analyst',
        '60000',
      ],
    ];

    const rows = data.map((row) => row.join(',')).join('\n');

    return this.includeHeaders ? `${headers}\n${rows}` : rows;
  }

  downloadCSV(content: string, fileName: string): void {
    const blob = new Blob([content], { type: 'text/csv' });
    this.downloadBlob(blob, fileName);
  }

  downloadFile(content: string, fileName: string): void {
    const blob = new Blob([content], { type: 'application/octet-stream' });
    this.downloadBlob(blob, fileName);
  }

  downloadBlob(blob: Blob, fileName: string): void {
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = fileName;
    a.click();
    window.URL.revokeObjectURL(url);
  }
}
