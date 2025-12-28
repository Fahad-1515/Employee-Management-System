import { Component, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { EmployeeService } from '../../../services/employee.service';

@Component({
  selector: 'app-import-dialog',
  templateUrl: './import-dialog.component.html',
  styleUrls: ['./import-dialog.component.css'],
})
export class ImportDialogComponent {
  selectedFile: File | null = null;
  previewData: any[] = [];
  loading = false;
  validRows = 0;
  invalidRows = 0;

  constructor(
    public dialogRef: MatDialogRef<ImportDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    private employeeService: EmployeeService,
    private snackBar: MatSnackBar
  ) {}

  onFileSelected(event: any): void {
    const file = event.target.files[0];
    if (file) {
      this.selectedFile = file;
      this.previewFile(file);
    }
  }

  previewFile(file: File): void {
    if (file.type === 'text/csv' || file.name.endsWith('.csv')) {
      this.parseCSV(file);
    } else if (
      file.type ===
        'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' ||
      file.name.endsWith('.xlsx')
    ) {
      this.snackBar.open(
        'Excel import coming soon! Using CSV for now.',
        'Close',
        {
          duration: 3000,
        }
      );
      // For now, we'll simulate Excel data
      this.simulatePreviewData();
    } else {
      this.snackBar.open(
        'Unsupported file format. Please use CSV or Excel.',
        'Close',
        {
          duration: 5000,
        }
      );
    }
  }

  parseCSV(file: File): void {
    const reader = new FileReader();
    reader.onload = (e: any) => {
      const content = e.target.result;
      const rows = content.split('\n');
      const headers = rows[0].split(',').map((h: string) => h.trim());

      this.previewData = rows
        .slice(1, Math.min(11, rows.length)) // Preview first 10 rows
        .map((row: string, index: number) => {
          if (!row.trim()) return null;

          const columns = row.split(',').map((c: string) => c.trim());
          const employee: any = {};
          const errors: string[] = [];

          headers.forEach((header: string, i: number) => {
            employee[header.toLowerCase()] = columns[i] || '';
          });

          // Validation
          if (!employee.firstname) errors.push('First name required');
          if (!employee.lastname) errors.push('Last name required');
          if (!employee.email || !this.isValidEmail(employee.email)) {
            errors.push('Valid email required');
          }
          if (!employee.department) errors.push('Department required');

          return {
            ...employee,
            errors: errors.length > 0 ? errors.join(', ') : null,
            rowNumber: index + 2,
          };
        })
        .filter((row: any) => row !== null);

      this.calculateValidation();
    };
    reader.readAsText(file);
  }

  simulatePreviewData(): void {
    this.previewData = [
      {
        firstname: 'John',
        lastname: 'Doe',
        email: 'john@example.com',
        department: 'IT',
        position: 'Software Engineer',
        salary: '75000',
        errors: null,
        rowNumber: 2,
      },
      {
        firstname: 'Jane',
        lastname: 'Smith',
        email: 'jane@example.com',
        department: 'HR',
        position: 'HR Manager',
        salary: '65000',
        errors: null,
        rowNumber: 3,
      },
      {
        firstname: 'Bob',
        lastname: 'Johnson',
        email: 'invalid-email',
        department: 'Finance',
        position: 'Financial Analyst',
        salary: '60000',
        errors: 'Valid email required',
        rowNumber: 4,
      },
    ];

    this.calculateValidation();
  }

  calculateValidation(): void {
    this.validRows = this.previewData.filter((row) => !row.errors).length;
    this.invalidRows = this.previewData.filter((row) => row.errors).length;
  }

  isValidEmail(email: string): boolean {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
  }

  downloadTemplate(): void {
    const template = `firstname,lastname,email,department,position,salary
John,Doe,john@example.com,IT,Software Engineer,75000
Jane,Smith,jane@example.com,HR,HR Manager,65000
Mike,Johnson,mike@example.com,Finance,Financial Analyst,60000`;

    const blob = new Blob([template], { type: 'text/csv' });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = 'employee_import_template.csv';
    a.click();
    window.URL.revokeObjectURL(url);

    this.snackBar.open('Template downloaded successfully!', 'Close', {
      duration: 3000,
    });
  }

  confirmImport(): void {
    if (this.validRows === 0) {
      this.snackBar.open('No valid rows to import', 'Close', {
        duration: 3000,
      });
      return;
    }

    this.loading = true;

    // Simulate API call
    setTimeout(() => {
      this.snackBar.open(
        `Successfully imported ${this.validRows} employees`,
        'Close',
        {
          duration: 3000,
        }
      );
      this.loading = false;
      this.dialogRef.close({ success: true, imported: this.validRows });
    }, 1500);
  }

  clearFile(): void {
    this.selectedFile = null;
    this.previewData = [];
    this.validRows = 0;
    this.invalidRows = 0;
  }
}
