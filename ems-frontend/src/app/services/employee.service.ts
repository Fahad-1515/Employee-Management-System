import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpErrorResponse } from '@angular/common/http';
import { Observable, of, throwError } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';
import { environment } from '../environments/environment'; 
import {
  Employee,
  EmployeeResponse,
  EmployeeSearchCriteria,
} from '../models/employee.model';

@Injectable({
  providedIn: 'root',
})
export class EmployeeService {
  // UPDATED: Use environment configuration
  private apiUrl = `${environment.apiUrl}/employees`;
  private exportApiUrl = `${environment.apiUrl}/export`;

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
    console.log('🔧 EmployeeService initialized');
    console.log('🌐 Environment:', environment.production ? 'PRODUCTION' : 'DEVELOPMENT');
    console.log('🌐 API Base URL:', this.apiUrl);
    console.log('🌐 Full URLs:');
    console.log('  - Departments:', `${environment.apiUrl}/employees/departments`);
    console.log('  - Positions:', `${environment.apiUrl}/employees/positions`);
  }

  getDepartments(): Observable<string[]> {
    // UPDATED: Use environment.apiUrl directly
    const url = `${environment.apiUrl}/employees/departments`;
    console.log('🔄 Fetching departments from:', url);
    
    return this.http.get<string[]>(url).pipe(
      tap(data => {
        console.log('✅ Departments API success:', data);
        if (!data || data.length === 0) {
          console.warn('⚠️  API returned empty departments, using fallback');
        }
      }),
      catchError((error: HttpErrorResponse) => {
        console.error('❌ Departments API error:', {
          status: error.status,
          message: error.message,
          url: error.url,
          error: error.error
        });
        console.log('📋 Using fallback departments:', this.fallbackDepartments);
        return of(this.fallbackDepartments);
      })
    );
  }

  getPositions(): Observable<string[]> {
    // UPDATED: Use environment.apiUrl directly
    const url = `${environment.apiUrl}/employees/positions`;
    console.log('🔄 Fetching positions from:', url);
    
    return this.http.get<string[]>(url).pipe(
      tap(data => {
        console.log('✅ Positions API success:', data);
        if (!data || data.length === 0) {
          console.warn('⚠️  API returned empty positions, using fallback');
        }
      }),
      catchError((error: HttpErrorResponse) => {
        console.error('❌ Positions API error:', {
          status: error.status,
          message: error.message,
          url: error.url,
          error: error.error
        });
        console.log('📋 Using fallback positions:', this.fallbackPositions);
        return of(this.fallbackPositions);
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

    console.log('🔄 Fetching employees from:', this.apiUrl);
    console.log('📋 Params:', params.toString());

    return this.http.get<EmployeeResponse>(this.apiUrl, { params }).pipe(
      tap(response => {
        console.log('✅ Employees API response:', {
          hasContent: response.content?.length > 0,
          totalElements: response.totalElements, 
          totalPages: response.totalPages,
          contentLength: response.content?.length
        });
      }),
      catchError((error: HttpErrorResponse) => {
        console.error('❌ Search employees error:', {
          status: error.status,
          message: error.message,
          url: error.url,
          error: error.error
        });
        
        const errorResponse: EmployeeResponse = {
          content: [],
          number: 0,              
          size: size,            
          totalElements: 0,       
          totalPages: 0,
          hasNext: false,        
          hasPrevious: false     
        };
        return of(errorResponse);
      })
    );
  }

  getEmployeeById(id: number): Observable<Employee> {
    const url = `${this.apiUrl}/${id}`;
    console.log('🔄 Fetching employee:', url);
    
    return this.http.get<Employee>(url).pipe(
      catchError(error => {
        console.error(`❌ Get employee ${id} error:`, {
          status: error.status,
          message: error.message,
          error: error.error
        });
        return throwError(() => new Error('Employee not found'));
      })
    );
  }

  createEmployee(employee: Employee): Observable<Employee> {
    console.log('🔄 Creating employee at:', this.apiUrl);
    console.log('📋 Employee data:', employee);
    
    return this.http.post<Employee>(this.apiUrl, employee).pipe(
      tap(response => {
        console.log('✅ Employee created:', response);
        console.log('📝 Response details:', {
          id: response.id,
          name: `${response.firstName} ${response.lastName}`,
          email: response.email,
          department: response.department,
          position: response.position
        });
      }),
      catchError(error => {
        console.error('❌ Create employee error:', {
          status: error.status,
          message: error.message,
          error: error.error
        });
        return throwError(() => new Error('Failed to create employee'));
      })
    );
  }

  updateEmployee(id: number, employee: Employee): Observable<Employee> {
    const url = `${this.apiUrl}/${id}`;
    console.log('🔄 Updating employee at:', url);
    console.log('📋 Employee data:', employee);
    
    return this.http.put<Employee>(url, employee).pipe(
      tap(response => {
        console.log('✅ Employee updated:', response);
        console.log('📝 Response details:', {
          id: response.id,
          name: `${response.firstName} ${response.lastName}`,
          department: response.department,
          position: response.position
        });
      }),
      catchError(error => {
        console.error(`❌ Update employee ${id} error:`, {
          status: error.status,
          message: error.message,
          error: error.error
        });
        return throwError(() => new Error('Failed to update employee'));
      })
    );
  }

  deleteEmployee(id: number): Observable<void> {
    const url = `${this.apiUrl}/${id}`;
    console.log('🔄 Deleting employee:', url);
    
    return this.http.delete<void>(url).pipe(
      tap(() => console.log(`✅ Employee ${id} deleted`)),
      catchError(error => {
        console.error(`❌ Delete employee ${id} error:`, {
          status: error.status,
          message: error.message,
          error: error.error
        });
        return throwError(() => new Error('Failed to delete employee'));
      })
    );
  }

  exportToCSV(): Observable<Blob> {
    const url = `${this.exportApiUrl}/employees/csv`;
    console.log('🔄 Exporting to CSV from:', url);
    
    return this.http.get(url, {
      responseType: 'blob',
    }).pipe(
      tap(() => console.log('✅ CSV export successful')),
      catchError(error => {
        console.error('❌ Export CSV error:', {
          status: error.status,
          message: error.message,
          error: error.error
        });
        return throwError(() => new Error('Failed to export CSV'));
      })
    );
  }

  exportToExcel(): Observable<Blob> {
    const url = `${this.exportApiUrl}/employees/excel`;
    console.log('🔄 Exporting to Excel from:', url);
    
    return this.http.get(url, {
      responseType: 'blob',
    }).pipe(
      tap(() => console.log('✅ Excel export successful')),
      catchError(error => {
        console.error('❌ Export Excel error:', {
          status: error.status,
          message: error.message,
          error: error.error
        });
        return throwError(() => new Error('Failed to export Excel'));
      })
    );
  }

  getDashboardStats(): Observable<any> {
    const url = `${this.apiUrl}/stats/summary`;
    console.log('🔄 Fetching dashboard stats from:', url);
    
    return this.http.get<any>(url).pipe(
      tap(stats => console.log('✅ Dashboard stats:', stats)),
      catchError(error => {
        console.error('❌ Dashboard stats error:', {
          status: error.status,
          message: error.message,
          error: error.error
        });
        return of({ totalEmployees: 0, totalDepartments: 0 });
      })
    );
  }

  getEmployeeStatistics(): Observable<any> {
    const url = `${this.apiUrl}/stats/summary`;
    console.log('🔄 Fetching employee statistics from:', url);
    
    return this.http.get<any>(url).pipe(
      tap(stats => console.log('✅ Employee statistics:', stats)),
      catchError(error => {
        console.error('❌ Employee statistics error:', {
          status: error.status,
          message: error.message,
          error: error.error
        });
        return of({ totalEmployees: 0, totalDepartments: 0 });
      })
    );
  }

  emailExists(email: string): Observable<boolean> {
    const url = `${this.apiUrl}/check-email?email=${encodeURIComponent(email)}`;
    console.log('🔄 Checking email existence:', url);
    
    return this.http.get<boolean>(url).pipe(
      tap(exists => console.log(`✅ Email ${email} exists:`, exists)),
      catchError(error => {
        console.error('❌ Email check error:', {
          status: error.status,
          message: error.message,
          error: error.error
        });
        return of(false);
      })
    );
  }

  getDepartmentCount(): Observable<number> {
    const url = `${this.apiUrl}/departments/count`;
    console.log('🔄 Fetching department count from:', url);
    
    return this.http.get<number>(url).pipe(
      tap(count => console.log('✅ Department count:', count)),
      catchError(error => {
        console.error('❌ Department count error:', {
          status: error.status,
          message: error.message,
          error: error.error
        });
        return of(0);
      })
    );
  }

  // NEW: Test backend connection
  testBackendConnection(): Observable<{success: boolean, url: string, data?: any, error?: any}> {
    const testUrl = `${environment.apiUrl}/employees/departments`;
    console.log('🔗 Testing backend connection to:', testUrl);
    
    return this.http.get<string[]>(testUrl).pipe(
      tap(data => {
        console.log('✅ Backend connection successful:', data);
      }),
      map(data => ({ success: true, url: testUrl, data })),
      catchError(error => {
        console.error('❌ Backend connection failed:', error);
        return of({ 
          success: false, 
          url: testUrl, 
          error: {
            status: error.status,
            message: error.message
          }
        });
      })
    );
  }
}
