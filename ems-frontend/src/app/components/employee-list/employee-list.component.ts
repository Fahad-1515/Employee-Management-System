import { Component, OnInit, ViewChild } from '@angular/core';
import { MatTableDataSource } from '@angular/material/table';
import { MatPaginator, PageEvent } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { EmployeeService } from '../../services/employee.service';
import { Employee, EmployeeSearchCriteria } from '../../models/employee.model';
import { EmployeeFormComponent } from '../employee-form/employee-form.component';

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

  onSearch(): void {
    this.currentPage = 0;
    this.loadEmployees();
  }

  onClearFilters(): void {
    this.searchCriteria = {};
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
  private downloadFile(
    blob: Blob,
    filename: string,
    contentType: string
  ): void {
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = filename;
    link.click();
    window.URL.revokeObjectURL(url);
  }

  toggleAdvancedSearch(): void {
    this.showAdvancedSearch = !this.showAdvancedSearch;
  }
}
