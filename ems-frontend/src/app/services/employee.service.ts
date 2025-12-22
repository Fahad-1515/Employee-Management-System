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

  // Helper to get headers with JWT token
  private getHeaders(): HttpHeaders {
    const token = this.authService.getToken();
    return new HttpHeaders({
      'Content-Type': 'application/json',
      ...(token ? { 'Authorization': `Bearer ${token}` } : {})
    });
  }


  getDepartments(): Observable<string[]> {
    const url = `${this.baseUrl}/employees/departments`;
    console.log('📞 Fetching departments from:', url);
    
    return this.http.get<string[]>(url, { headers: this.getHeaders() }).pipe(
      tap(data => console.log('✅ Departments received:', data)),
      catchError((error) => {
        console.error('❌ Departments error:', error);
        // Fallback data
        return of(['IT', 'HR', 'Finance', 'Marketing', 'Sales', 'Operations']);
      })
    );
  }

  getPositions(): Observable<string[]> {
    const url = `${this.baseUrl}/employees/positions`;
    console.log('📞 Fetching positions from:', url);
    
    return this.http.get<string[]>(url, { headers: this.getHeaders() }).pipe(
      tap(data => console.log('✅ Positions received:', data)),
      catchError((error) => {
        console.error('❌ Positions error:', error);
        // Fallback data
        return of(['Software Engineer', 'HR Manager', 'Financial Analyst', 'Marketing Specialist']);
      })
    );
  }


  createEmployee(employee: Employee): Observable<any> {
    const url = `${this.baseUrl}/employees`;
    console.log('📤 Creating employee at:', url);
    
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


  exportToExcel(): Observable<Blob> {
    const url = `${this.baseUrl}/export/employees/excel`;
    console.log('📊 Exporting Excel from:', url);
    
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

  getEmployeeById(id: number): Observable<Employee> {
    const url = `${this.baseUrl}/employees/${id}`;
    console.log('👤 Fetching employee:', url);
    
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


  getDashboardStats(): Observable<any> {
    const url = `${this.baseUrl}/employees/stats/summary`;
    console.log('📈 Fetching dashboard stats:', url);
    
    return this.http.get<any>(url, { 
      headers: this.getHeaders() 
    }).pipe(
      tap(stats => console.log('✅ Stats received:', stats)),
      catchError(error => {
        console.error('❌ Stats error:', error);
        return of({ totalEmployees: 0, totalDepartments: 0 });
      })
    );
  }
}
