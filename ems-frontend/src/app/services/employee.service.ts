import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpHeaders } from '@angular/common/http';
import { Observable, of, throwError } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';
import { Employee, EmployeeResponse, EmployeeSearchCriteria } from '../models/employee.model';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class EmployeeService {
  // ✅ Your working Render backend
  private baseUrl = 'https://employee-management-system-jxdj.onrender.com/api';
  
  constructor(
    private http: HttpClient, 
    private authService: AuthService
  ) {
    console.log('✅ EmployeeService connected to:', this.baseUrl);
  }

  // Helper to get headers with JWT token (for PROTECTED endpoints)
  private getHeaders(): HttpHeaders {
    const token = this.authService.getToken();
    return new HttpHeaders({
      'Content-Type': 'application/json',
      ...(token ? { 'Authorization': `Bearer ${token}` } : {})
    });
  }

  // Helper for PUBLIC endpoints (no auth header)
  private getPublicHeaders(): HttpHeaders {
    return new HttpHeaders({
      'Content-Type': 'application/json'
    });
  }

  // ========== PUBLIC ENDPOINTS (NO AUTH REQUIRED) ==========
  
  getDepartments(): Observable<string[]> {
    const url = `${this.baseUrl}/employees/departments`;
    console.log('📞 Fetching departments from:', url);
    
    // ✅ PUBLIC: Remove auth header
    return this.http.get<string[]>(url, { headers: this.getPublicHeaders() }).pipe(
      tap(data => {
        console.log('✅ Departments received:', data);
        console.log('Type:', typeof data);
        console.log('Is Array:', Array.isArray(data));
        console.log('Length:', data?.length);
      }),
      catchError((error) => {
        console.error('❌ Departments API error:', error);
        console.error('Status:', error.status);
        console.error('Message:', error.message);
        console.error('Full error:', error);
        
        // Fallback data
        return of([
          'IT', 'HR', 'Finance', 'Marketing', 
          'Sales', 'Operations', 'Support', 'Engineering'
        ]);
      })
    );
  }

  getPositions(): Observable<string[]> {
    const url = `${this.baseUrl}/employees/positions`;
    console.log('📞 Fetching positions from:', url);
    
    // ✅ PUBLIC: Remove auth header
    return this.http.get<string[]>(url, { headers: this.getPublicHeaders() }).pipe(
      tap(data => {
        console.log('✅ Positions received:', data);
        console.log('Type:', typeof data);
        console.log('Is Array:', Array.isArray(data));
        console.log('Length:', data?.length);
      }),
      catchError((error) => {
        console.error('❌ Positions API error:', error);
        console.error('Status:', error.status);
        console.error('Message:', error.message);
        
        // Fallback data
        return of([
          'Software Engineer', 'HR Manager', 'Financial Analyst',
          'Marketing Specialist', 'Sales Executive', 'Operations Manager',
          'System Administrator', 'Frontend Developer', 'Backend Developer'
        ]);
      })
    );
  }

  getDashboardStats(): Observable<any> {
    const url = `${this.baseUrl}/employees/stats/summary`;
    console.log('📈 Fetching dashboard stats:', url);
    
    // ✅ PUBLIC: Remove auth header
    return this.http.get<any>(url, { headers: this.getPublicHeaders() }).pipe(
      tap(stats => console.log('✅ Stats received:', stats)),
      catchError(error => {
        console.error('❌ Stats error:', error);
        return of({ 
          totalEmployees: 0, 
          totalDepartments: 0,
          avgSalary: 0,
          totalSalary: 0,
          recentHires: 0
        });
      })
    );
  }

  // ========== PROTECTED ENDPOINTS (REQUIRE AUTH) ==========
  
  searchEmployees(
    page: number = 0,
    size: number = 10,
    criteria: EmployeeSearchCriteria = {}
  ): Observable<EmployeeResponse> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (criteria.searchTerm) params = params.set('search', criteria.searchTerm);
    if (criteria.department) params = params.set('department', criteria.department);
    if (criteria.position) params = params.set('position', criteria.position);
    if (criteria.minSalary) params = params.set('minSalary', criteria.minSalary.toString());
    if (criteria.maxSalary) params = params.set('maxSalary', criteria.maxSalary.toString());

    const url = `${this.baseUrl}/employees`;
    console.log('🔍 Searching employees at:', url);
    
    // ⚠️ PROTECTED: Keep auth header (but you made this public in SecurityConfig)
    return this.http.get<EmployeeResponse>(url, {
      headers: this.getHeaders(),
      params
    }).pipe(
      tap(response => console.log('✅ Search results:', response.totalElements, 'employees')),
      catchError(error => {
        console.error('❌ Search error:', error);
        return of({
          content: [],
          totalElements: 0,
          totalPages: 0,
          size: size,
          number: page,
          hasNext: false,
          hasPrevious: false
        });
      })
    );
  }

  createEmployee(employee: Employee): Observable<any> {
    const url = `${this.baseUrl}/employees`;
    console.log('📤 Creating employee at:', url);
    
    // ✅ PROTECTED: Keep auth header
    return this.http.post(url, employee, { 
      headers: this.getHeaders() 
    }).pipe(
      tap(response => console.log('✅ Employee created:', response)),
      catchError(error => {
        console.error('❌ Create error:', error);
        return throwError(() => error);
      })
    );
  }

  updateEmployee(id: number, employee: Employee): Observable<any> {
    const url = `${this.baseUrl}/employees/${id}`;
    console.log('📤 Updating employee at:', url);
    
    // ✅ PROTECTED: Keep auth header
    return this.http.put(url, employee, { 
      headers: this.getHeaders() 
    }).pipe(
      tap(response => console.log('✅ Employee updated:', response)),
      catchError(error => {
        console.error('❌ Update error:', error);
        return throwError(() => error);
      })
    );
  }

  deleteEmployee(id: number): Observable<void> {
    const url = `${this.baseUrl}/employees/${id}`;
    console.log('🗑️ Deleting employee at:', url);
    
    // ✅ PROTECTED: Keep auth header
    return this.http.delete<void>(url, { 
      headers: this.getHeaders() 
    }).pipe(
      tap(() => console.log('✅ Employee deleted:', id)),
      catchError(error => {
        console.error('❌ Delete error:', error);
        return throwError(() => error);
      })
    );
  }

  getEmployeeById(id: number): Observable<Employee> {
    const url = `${this.baseUrl}/employees/${id}`;
    console.log('👤 Fetching employee:', url);
    
    // ⚠️ PROTECTED: Keep auth header
    return this.http.get<Employee>(url, { 
      headers: this.getHeaders() 
    }).pipe(
      tap(employee => console.log('✅ Employee found:', employee.firstName, employee.lastName)),
      catchError(error => {
        console.error('❌ Get employee error:', error);
        return throwError(() => error);
      })
    );
  }

  exportToExcel(): Observable<Blob> {
    const url = `${this.baseUrl}/export/employees/excel`;
    console.log('📊 Exporting Excel from:', url);
    
    // ✅ PROTECTED: Keep auth header
    return this.http.get(url, {
      responseType: 'blob',
      headers: this.getHeaders()
    }).pipe(
      tap(() => console.log('✅ Excel export successful')),
      catchError(error => {
        console.error('❌ Excel export error:', error);
        return throwError(() => error);
      })
    );
  }

  exportToCSV(): Observable<Blob> {
    const url = `${this.baseUrl}/export/employees/csv`;
    console.log('📊 Exporting CSV from:', url);
    
    // ✅ PROTECTED: Keep auth header
    return this.http.get(url, {
      responseType: 'blob',
      headers: this.getHeaders()
    }).pipe(
      tap(() => console.log('✅ CSV export successful')),
      catchError(error => {
        console.error('❌ CSV export error:', error);
        return throwError(() => error);
      })
    );
  }
}
