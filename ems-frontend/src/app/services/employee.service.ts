import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpErrorResponse } from '@angular/common/http';
import { Observable, of, throwError } from 'rxjs';
import { catchError, tap, map } from 'rxjs/operators';
import {
  Employee,
  EmployeeResponse,
  EmployeeSearchCriteria,
} from '../models/employee.model';

@Injectable({
  providedIn: 'root',
})
export class EmployeeService {
  private apiUrl = '/api/employees';  
  private exportApiUrl = '/api/export';

  // FALLBACK DATA for when API fails
  private fallbackDepartments = [
    'IT', 'HR', 'Finance', 'Marketing', 
    'Sales', 'Operations', 'R&D', 'Support'
  ];
  
  private fallbackPositions = [
    'Software Engineer', 'HR Manager', 'Financial Analyst', 
    'Marketing Specialist', 'Sales Manager', 'Operations Manager',
    'System Administrator', 'Frontend Developer', 'Backend Developer',
    'UI/UX Designer', 'Data Analyst', 'Project Manager'
  ];

  constructor(private http: HttpClient) {
    console.log(' EmployeeService initialized');
    console.log(' API Base URL:', this.apiUrl);
  }

  getDepartments(): Observable<string[]> {
    console.log(' Fetching departments from:', `${this.apiUrl}/departments`);
    
    return this.http.get<string[]>(`${this.apiUrl}/departments`).pipe(
      tap(data => {
        console.log(' Departments API success:', data);
        if (!data || data.length === 0) {
          console.warn('  API returned empty departments, using fallback');
        }
      }),
      catchError((error: HttpErrorResponse) => {
        console.error(' Departments API error:', {
          status: error.status,
          message: error.message,
          url: error.url
        });
        console.log(' Using fallback departments:', this.fallbackDepartments);
        return of(this.fallbackDepartments); // Return fallback data
      })
    );
  }

  // ========== FIXED POSITION METHOD ==========
  getPositions(): Observable<string[]> {
    console.log('Fetching positions from:', `${this.apiUrl}/positions`);
    
    return this.http.get<string[]>(`${this.apiUrl}/positions`).pipe(
      tap(data => {
        console.log(' Positions API success:', data);
        if (!data || data.length === 0) {
          console.warn('  API returned empty positions, using fallback');
        }
      }),
      catchError((error: HttpErrorResponse) => {
        console.error(' Positions API error:', {
          status: error.status,
          message: error.message,
          url: error.url
        });
        console.log('📋 Using fallback positions:', this.fallbackPositions);
        return of(this.fallbackPositions); // Return fallback data
      })
    );
  }

  // ========== FIXED SEARCH METHOD (TypeScript error fixed) ==========
  searchEmployees(
    page: number = 0,
    size: number = 10,
    criteria: EmployeeSearchCriteria = {}
  ): Observable<EmployeeResponse> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (criteria.searchTerm) {
      params = params.set('search', criteria.searchTerm);
    }
    if (criteria.department) {
      params = params.set('department', criteria.department);
    }
    if (criteria.position) {
      params = params.set('position', criteria.position);
    }
    if (criteria.minSalary) {
      params = params.set('minSalary', criteria.minSalary.toString());
    }
    if (criteria.maxSalary) {
      params = params.set('maxSalary', criteria.maxSalary.toString());
    }

    console.log(' Fetching employees with params:', params.toString());

    return this.http.get<EmployeeResponse>(this.apiUrl, { params }).pipe(
      tap(response => {
        console.log(' Employees API response:', {
          hasContent: response.content?.length > 0,
          totalElements: response.totalItems, // Using totalItems from interface
          totalPages: response.totalPages
        });
      }),
      catchError((error: HttpErrorResponse) => {
        console.error(' Search employees error:', {
          status: error.status,
          message: error.message,
          url: error.url
        });
        
        // Return empty response that matches EmployeeResponse interface EXACTLY
        const errorResponse: EmployeeResponse = {
          content: [],
          currentPage: 0,
          totalItems: 0,
          totalPages: 0,
          pageSize: size,
          hasNext: false,
          hasPrevious: false
        };
        return of(errorResponse);
      })
    );
  }

  getEmployeeById(id: number): Observable<Employee> {
    return this.http.get<Employee>(`${this.apiUrl}/${id}`).pipe(
      catchError(error => {
        console.error(` Get employee ${id} error:`, error);
        return throwError(() => new Error('Employee not found'));
      })
    );
  }

  createEmployee(employee: Employee): Observable<Employee> {
    return this.http.post<Employee>(this.apiUrl, employee).pipe(
      tap(response => console.log(' Employee created:', response)),
      catchError(error => {
        console.error(' Create employee error:', error);
        return throwError(() => new Error('Failed to create employee'));
      })
    );
  }

  updateEmployee(id: number, employee: Employee): Observable<Employee> {
    return this.http.put<Employee>(`${this.apiUrl}/${id}`, employee).pipe(
      tap(response => console.log(' Employee updated:', response)),
      catchError(error => {
        console.error(` Update employee ${id} error:`, error);
        return throwError(() => new Error('Failed to update employee'));
      })
    );
  }

  deleteEmployee(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`).pipe(
      tap(() => console.log(` Employee ${id} deleted`)),
      catchError(error => {
        console.error(` Delete employee ${id} error:`, error);
        return throwError(() => new Error('Failed to delete employee'));
      })
    );
  }

  // Other methods remain the same...
  exportToCSV(): Observable<Blob> {
    return this.http.get(`${this.exportApiUrl}/employees/csv`, {
      responseType: 'blob',
    }).pipe(
      catchError(error => {
        console.error(' Export CSV error:', error);
        return throwError(() => new Error('Failed to export CSV'));
      })
    );
  }

  exportToExcel(): Observable<Blob> {
    return this.http.get(`${this.exportApiUrl}/employees/excel`, {
      responseType: 'blob',
    }).pipe(
      catchError(error => {
        console.error(' Export Excel error:', error);
        return throwError(() => new Error('Failed to export Excel'));
      })
    );
  }

  getDashboardStats(): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/stats/summary`).pipe(
      catchError(error => {
        console.error(' Dashboard stats error:', error);
        return of({ totalEmployees: 0, totalDepartments: 0 });
      })
    );
  }

  getEmployeeStatistics(): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/stats/summary`).pipe(
      catchError(error => {
        console.error(' Employee statistics error:', error);
        return of({ totalEmployees: 0, totalDepartments: 0 });
      })
    );
  }

  emailExists(email: string): Observable<boolean> {
    return this.http.get<boolean>(`${this.apiUrl}/check-email?email=${email}`).pipe(
      catchError(error => {
        console.error(' Email check error:', error);
        return of(false); // Return false on error
      })
    );
  }

  getDepartmentCount(): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/departments/count`).pipe(
      catchError(error => {
        console.error(' Department count error:', error);
        return of(0); // Return 0 on error
      })
    );
  }
}
