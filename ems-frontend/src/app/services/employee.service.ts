import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpHeaders } from '@angular/common/http';
import { Observable, of, throwError } from 'rxjs';
import { catchError, tap, delay } from 'rxjs/operators';
import { Employee, EmployeeResponse, EmployeeSearchCriteria } from '../models/employee.model';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class EmployeeService {
  private baseUrl = 'https://employee-management-system-jxdj.onrender.com/api';
  
  constructor(private http: HttpClient, private authService: AuthService) {
    console.log('🔄 EmployeeService initialized');
  }

  private getHeaders(): HttpHeaders {
    const token = this.authService.getToken();
    return new HttpHeaders({
      'Content-Type': 'application/json',
      ...(token ? { 'Authorization': `Bearer ${token}` } : {})
    });
  }

  getDepartments(): Observable<string[]> {
    const url = `${this.baseUrl}/employees/departments`;
    console.log('📞 Calling:', url);
    
    return this.http.get<string[]>(url, { headers: this.getHeaders() }).pipe(
      tap(data => console.log('✅ Departments response:', data)),
      catchError(error => {
        console.error('❌ Departments error:', error);
        // Return fallback data
        return of([
          'IT', 'HR', 'Finance', 'Marketing',
          'Sales', 'Operations', 'Support'
        ]);
      })
    );
  }

  getPositions(): Observable<string[]> {
    const url = `${this.baseUrl}/employees/positions`;
    console.log('📞 Calling:', url);
    
    return this.http.get<string[]>(url, { headers: this.getHeaders() }).pipe(
      tap(data => console.log('✅ Positions response:', data)),
      catchError(error => {
        console.error('❌ Positions error:', error);
        // Return fallback data
        return of([
          'Software Engineer', 'HR Manager', 'Financial Analyst',
          'Marketing Specialist', 'Sales Manager', 'Operations Manager'
        ]);
      })
    );
  }

  createEmployee(employee: Employee): Observable<any> {
    const url = `${this.baseUrl}/employees`;
    console.log('📤 Creating employee:', employee);
    
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
    console.log('📤 Updating employee:', employee);
    
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

    return this.http.get<EmployeeResponse>(`${this.baseUrl}/employees`, {
      headers: this.getHeaders(),
      params
    }).pipe(
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
}
