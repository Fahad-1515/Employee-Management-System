import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
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
  private exportApiUrl = '/api/export'; // Separate URL for exports

  constructor(private http: HttpClient) {}

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

    return this.http.get<EmployeeResponse>(this.apiUrl, { params });
  }

  getEmployeeById(id: number): Observable<Employee> {
    return this.http.get<Employee>(`${this.apiUrl}/${id}`);
  }

  createEmployee(employee: Employee): Observable<Employee> {
    return this.http.post<Employee>(this.apiUrl, employee);
  }

  updateEmployee(id: number, employee: Employee): Observable<Employee> {
    return this.http.put<Employee>(`${this.apiUrl}/${id}`, employee);
  }

  deleteEmployee(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  getDepartments(): Observable<string[]> {
    return this.http.get<string[]>(`${this.apiUrl}/departments`);
  }

  getPositions(): Observable<string[]> {
    return this.http.get<string[]>(`${this.apiUrl}/positions`);
  }

  // CORRECTED: Use the exportApiUrl instead of apiUrl for export endpoints
  exportToCSV(): Observable<Blob> {
    return this.http.get(`${this.exportApiUrl}/employees/csv`, {
      responseType: 'blob',
    });
  }

  exportToExcel(): Observable<Blob> {
    return this.http.get(`${this.exportApiUrl}/employees/excel`, {
      responseType: 'blob',
    });
  }
  getDashboardStats(): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/stats`);
  }
  getEmployeeStatistics(): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/statistics`);
  }

  emailExists(email: string): Observable<boolean> {
    return this.http.get<boolean>(`${this.apiUrl}/check-email?email=${email}`);
  }

  // If you want real department count
  getDepartmentCount(): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/departments/count`);
  }
}
