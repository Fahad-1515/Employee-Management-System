import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';
import {
  LeaveRequest,
  LeaveBalance,
  LeavePolicy,
  LeaveStats,
} from '../models/leave.model';
import { AuthService } from './auth.service';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class LeaveService {
  private baseUrl = environment.apiUrl + '/leave';

  constructor(private http: HttpClient, private authService: AuthService) {}

  private getHeaders() {
    const token = this.authService.getToken();
    return {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    };
  }

  // Leave Requests
  getLeaveRequests(
    page: number = 0,
    size: number = 10,
    status?: string
  ): Observable<any> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (status) params = params.set('status', status);

    return this.http
      .get<any>(`${this.baseUrl}/requests`, {
        headers: this.getHeaders(),
        params,
      })
      .pipe(catchError(() => of({ content: [], totalElements: 0 })));
  }

  getMyLeaveRequests(): Observable<LeaveRequest[]> {
    return this.http
      .get<LeaveRequest[]>(`${this.baseUrl}/my-requests`, {
        headers: this.getHeaders(),
      })
      .pipe(catchError(() => of([])));
  }

  requestLeave(leaveRequest: Partial<LeaveRequest>): Observable<LeaveRequest> {
    return this.http.post<LeaveRequest>(
      `${this.baseUrl}/request`,
      leaveRequest,
      {
        headers: this.getHeaders(),
      }
    );
  }

  updateLeaveStatus(
    id: number,
    status: string,
    comments?: string
  ): Observable<any> {
    return this.http.put(
      `${this.baseUrl}/requests/${id}/status`,
      { status, comments },
      {
        headers: this.getHeaders(),
      }
    );
  }

  cancelLeaveRequest(id: number): Observable<any> {
    return this.http.put(
      `${this.baseUrl}/requests/${id}/cancel`,
      {},
      {
        headers: this.getHeaders(),
      }
    );
  }

  // Leave Balance
  getLeaveBalance(employeeId?: number): Observable<LeaveBalance> {
    const id = employeeId || 'my';
    return this.http
      .get<LeaveBalance>(`${this.baseUrl}/balance/${id}`, {
        headers: this.getHeaders(),
      })
      .pipe(catchError(() => of(this.getDefaultBalance())));
  }

  // Team Calendar
  getTeamCalendar(
    startDate: string,
    endDate: string
  ): Observable<LeaveRequest[]> {
    const params = new HttpParams()
      .set('startDate', startDate)
      .set('endDate', endDate);

    return this.http
      .get<LeaveRequest[]>(`${this.baseUrl}/calendar`, {
        headers: this.getHeaders(),
        params,
      })
      .pipe(catchError(() => of([])));
  }

  // Leave Policy
  getLeavePolicy(): Observable<LeavePolicy> {
    return this.http
      .get<LeavePolicy>(`${this.baseUrl}/policy`, {
        headers: this.getHeaders(),
      })
      .pipe(catchError(() => of(this.getDefaultPolicy())));
  }

  updateLeavePolicy(policy: LeavePolicy): Observable<LeavePolicy> {
    return this.http.put<LeavePolicy>(`${this.baseUrl}/policy`, policy, {
      headers: this.getHeaders(),
    });
  }

  // Statistics
  getLeaveStats(): Observable<LeaveStats> {
    return this.http
      .get<LeaveStats>(`${this.baseUrl}/stats`, {
        headers: this.getHeaders(),
      })
      .pipe(
        catchError(() =>
          of({
            pendingRequests: 0,
            approvedThisMonth: 0,
            rejectedThisMonth: 0,
            totalLeavesTaken: 0,
            averageLeaveDuration: 0,
          })
        )
      );
  }

  // Helper methods for mock data
  private getDefaultBalance(): LeaveBalance {
    return {
      employeeId: 0,
      vacationDays: 20,
      sickDays: 10,
      personalDays: 5,
      maternityDays: 90,
      paternityDays: 14,
      unpaidDays: 0,
      usedVacation: 5,
      usedSick: 2,
      usedPersonal: 1,
    };
  }

  private getDefaultPolicy(): LeavePolicy {
    return {
      vacationDays: 20,
      sickDays: 10,
      personalDays: 5,
      maternityDays: 90,
      paternityDays: 14,
      maxConsecutiveDays: 30,
      advanceNoticeDays: 3,
      carryOverEnabled: true,
      maxCarryOverDays: 10,
    };
  }
}
