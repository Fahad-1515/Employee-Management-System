import { Component, OnInit, ViewChild } from '@angular/core';
import { MatTableDataSource } from '@angular/material/table';
import { MatPaginator, PageEvent } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { EmployeeService } from '../../services/employee.service';
import { Employee, EmployeeSearchCriteria } from '../../models/employee.model';
import { EmployeeFormComponent } from '../employee-form/employee-form.component';
import { ImportDialogComponent } from '../shared/import-dialog/import-dialog.component';
import { ExportDialogComponent } from '../shared/export-dialog/export-dialog.component';

@Component({
  selector: 'app-employee-list',
  templateUrl: './employee-list.component.html',
  styleUrls: ['./employee-list.component.css'],
})
export class EmployeeListComponent implements OnInit {
  displayedColumns: string[] = [
    'id',
    'firstName',
    'lastName',
    'email',
    'department',
    'position',
    'salary',
    'actions',
  ];

  dataSource = new MatTableDataSource<Employee>();
  loading = false;

  // Advanced Search
  searchCriteria: EmployeeSearchCriteria = {};
  departments: string[] = [];
  positions: string[] = [];
  showAdvancedSearch = false;

  // Pagination
  totalElements = 0;
  pageSize = 10;
  currentPage = 0;

  // Bulk Operations
  selectedEmployees = new Set<number>();
  showBulkToolbar = false;

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  constructor(
    private employeeService: EmployeeService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.loadEmployees();
    this.loadFilters();
  }

  ngAfterViewInit(): void {
    this.dataSource.paginator = this.paginator;
    this.dataSource.sort = this.sort;
  }

  loadEmployees(): void {
    this.loading = true;
    this.employeeService
      .searchEmployees(this.currentPage, this.pageSize, this.searchCriteria)
      .subscribe({
        next: (response) => {
          this.dataSource.data = response.content;
          this.totalElements = response.totalElements;
          this.loading = false;
        },
        error: (error) => {
          this.loading = false;
          this.snackBar.open('Error loading employees', 'Close', {
            duration: 5000,
          });
        },
      });
  }

  loadFilters(): void {
    this.employeeService.getDepartments().subscribe({
      next: (departments) => {
        this.departments = departments;
      },
      error: (error) => {
        console.error('Error loading departments:', error);
        // Fallback data
        this.departments = [
          'IT',
          'HR',
          'Finance',
          'Marketing',
          'Sales',
          'Operations',
        ];
      },
    });

    this.employeeService.getPositions().subscribe({
      next: (positions) => {
        this.positions = positions;
      },
      error: (error) => {
        console.error('Error loading positions:', error);
        // Fallback data
        this.positions = [
          'Software Engineer',
          'HR Manager',
          'Financial Analyst',
          'Marketing Specialist',
          'Sales Manager',
          'Operations Manager',
          'System Administrator',
          'Frontend Developer',
        ];
      },
    });
  }

  openImportDialog(): void {
    const dialogRef = this.dialog.open(ImportDialogComponent, {
      width: '850px',
      maxHeight: '90vh',
      disableClose: true,
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result?.success) {
        this.snackBar.open(
          `Successfully imported ${result.imported} employees`,
          'Close',
          { duration: 3000 }
        );
        this.loadEmployees(); // Refresh the list
      }
    });
  }

  openExportDialog(): void {
    const dialogRef = this.dialog.open(ExportDialogComponent, {
      width: '700px',
      data: { defaultFormat: 'excel' },
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result?.success) {
        this.snackBar.open('Export completed successfully!', 'Close', {
          duration: 3000,
        });
      }
    });
  }

  onSearch(): void {
    this.currentPage = 0;
    this.loadEmployees();
  }

  onClearFilters(): void {
    this.searchCriteria = {};
    this.selectedEmployees.clear();
    this.showBulkToolbar = false;
    this.onSearch();
  }

  onPageChange(event: PageEvent): void {
    this.currentPage = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadEmployees();
  }

  openEmployeeForm(employee?: Employee): void {
    const dialogRef = this.dialog.open(EmployeeFormComponent, {
      width: '600px',
      data: employee,
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.loadEmployees();
      }
    });
  }

  deleteEmployee(employee: Employee): void {
    if (
      confirm(
        `Are you sure you want to delete ${employee.firstName} ${employee.lastName}?`
      )
    ) {
      this.employeeService.deleteEmployee(employee.id!).subscribe({
        next: () => {
          this.snackBar.open('Employee deleted successfully', 'Close', {
            duration: 3000,
          });
          this.loadEmployees();
        },
        error: (error) => {
          this.snackBar.open('Error deleting employee', 'Close', {
            duration: 5000,
          });
        },
      });
    }
  }

  exportToCSV(): void {
    this.employeeService.exportToCSV().subscribe({
      next: (blob: Blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `employees_${Date.now()}.csv`;
        a.click();
        window.URL.revokeObjectURL(url);
        this.snackBar.open('CSV exported successfully!', 'Close', {
          duration: 3000,
        });
      },
      error: (error) => {
        this.snackBar.open('Error exporting CSV', 'Close', { duration: 5000 });
      },
    });
  }

  exportToExcel(): void {
    this.employeeService.exportToExcel().subscribe({
      next: (blob: Blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `employees_${Date.now()}.xlsx`;
        a.click();
        window.URL.revokeObjectURL(url);
        this.snackBar.open('Excel exported successfully!', 'Close', {
          duration: 3000,
        });
      },
      error: (error) => {
        this.snackBar.open('Error exporting Excel', 'Close', {
          duration: 5000,
        });
      },
    });
  }

  toggleAdvancedSearch(): void {
    this.showAdvancedSearch = !this.showAdvancedSearch;
  }

  // Bulk Operations Methods
  toggleSelection(id: number): void {
    if (this.selectedEmployees.has(id)) {
      this.selectedEmployees.delete(id);
    } else {
      this.selectedEmployees.add(id);
    }
    this.updateBulkToolbar();
  }

  isSelected(id: number): boolean {
    return this.selectedEmployees.has(id);
  }

  selectAll(): void {
    if (this.selectedEmployees.size === this.dataSource.data.length) {
      this.selectedEmployees.clear();
    } else {
      this.dataSource.data.forEach((employee) => {
        if (employee.id) this.selectedEmployees.add(employee.id);
      });
    }
    this.updateBulkToolbar();
  }

  updateBulkToolbar(): void {
    this.showBulkToolbar = this.selectedEmployees.size > 0;
  }

  clearSelection(): void {
    this.selectedEmployees.clear();
    this.showBulkToolbar = false;
  }
  getDisplayedColumnsWithSelect(): string[] {
    return ['select', ...this.displayedColumns];
  }
  bulkDelete(): void {
    const count = this.selectedEmployees.size;
    if (count === 0 || !confirm(`Delete ${count} selected employees?`)) return;

    const employeeIds = Array.from(this.selectedEmployees);
    this.employeeService.bulkDelete(employeeIds).subscribe({
      next: () => {
        this.snackBar.open(`Deleted ${count} employees successfully`, 'Close', {
          duration: 3000,
        });
        this.clearSelection();
        this.loadEmployees();
      },
      error: () => {
        this.snackBar.open('Error deleting employees', 'Close', {
          duration: 5000,
        });
      },
    });
  }

  exportSelected(): void {
    if (this.selectedEmployees.size === 0) return;

    // Create CSV content
    const headers = [
      'ID',
      'First Name',
      'Last Name',
      'Email',
      'Department',
      'Position',
      'Salary',
    ];
    const selectedData = this.dataSource.data.filter(
      (emp) => emp.id && this.selectedEmployees.has(emp.id)
    );

    const csvContent = [
      headers.join(','),
      ...selectedData.map((emp) =>
        [
          emp.id,
          `"${emp.firstName}"`,
          `"${emp.lastName}"`,
          `"${emp.email}"`,
          `"${emp.department}"`,
          `"${emp.position}"`,
          emp.salary,
        ].join(',')
      ),
    ].join('\n');

    const blob = new Blob([csvContent], { type: 'text/csv' });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `selected_employees_${Date.now()}.csv`;
    a.click();
    window.URL.revokeObjectURL(url);

    this.snackBar.open(`Exported ${selectedData.length} employees`, 'Close', {
      duration: 3000,
    });
  }
}
