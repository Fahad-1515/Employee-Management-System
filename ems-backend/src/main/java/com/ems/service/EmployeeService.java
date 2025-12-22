import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpErrorResponse } from '@angular/common/http';
import { Observable, of, throwError } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';
import {
  Employee,
  EmployeeResponse,
  EmployeeSearchCriteria,
} from '../models/employee.model';

@Injectable({
  providedIn: 'root',
})
export class EmployeeService {
  // HARDCODED FOR VERCEL - NO ENVIRONMENT FILES NEEDED
  private apiUrl = 'https://employee-management-system-jxdj.onrender.com/api/employees';
  private exportApiUrl = 'https://employee-management-system-jxdj.onrender.com/api/export';

  constructor(private http: HttpClient) {
    console.log('🔧 EmployeeService initialized - HARDCODED URL');
  }

  getDepartments(): Observable<string[]> {
    return this.http.get<string[]>(`${this.apiUrl}/departments`).pipe(
      catchError(() => of(['IT', 'HR', 'Finance', 'Marketing', 'Sales', 'Operations']))
    );
  }

  getPositions(): Observable<string[]> {
    return this.http.get<string[]>(`${this.apiUrl}/positions`).pipe(
      catchError(() => of(['Software Engineer', 'HR Manager', 'Financial Analyst', 'Marketing Specialist']))
    );
  }

  // Add other essential methods...
}
