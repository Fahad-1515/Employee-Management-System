import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LoginComponent } from './components/login/login.component';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { EmployeeListComponent } from './components/employee-list/employee-list.component';
import { AnalyticsComponent } from './components/analytics/analytics.component';
import { EmployeeFormComponent } from './components/employee-form/employee-form.component';
import { EmployeeProfileComponent } from './components/employee-profile/employee-profile.component';
import { LeaveRequestComponent } from './components/leave/leave-request/leave-request.component';
import { LeaveManagementComponent } from './components/leave/leave-management/leave-management.component';
import { LeaveCalendarComponent } from './components/leave/leave-calendar/leave-calendar.component';
import { AuthGuard } from './guards/auth.guard';

const routes: Routes = [
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },

  // Main routes
  {
    path: 'dashboard',
    component: DashboardComponent,
    canActivate: [AuthGuard],
  },
  {
    path: 'employees',
    component: EmployeeListComponent,
    canActivate: [AuthGuard],
  },
  {
    path: 'analytics',
    component: AnalyticsComponent,
    canActivate: [AuthGuard],
  },

  // Employee Management
  {
    path: 'employee/add',
    component: EmployeeFormComponent,
    canActivate: [AuthGuard],
    data: { isEdit: false },
  },
  {
    path: 'employee/edit/:id',
    component: EmployeeFormComponent,
    canActivate: [AuthGuard],
    data: { isEdit: true },
  },

  // Employee Profile
  {
    path: 'profile',
    component: EmployeeProfileComponent,
    canActivate: [AuthGuard],
  },
  {
    path: 'profile/:id',
    component: EmployeeProfileComponent,
    canActivate: [AuthGuard],
  },

  // Leave Management
  {
    path: 'leave/request',
    component: LeaveRequestComponent,
    canActivate: [AuthGuard],
  },
  {
    path: 'leave/manage',
    component: LeaveManagementComponent,
    canActivate: [AuthGuard],
  },
  {
    path: 'leave/calendar',
    component: LeaveCalendarComponent,
    canActivate: [AuthGuard],
  },

  // Fallback route
  { path: '**', redirectTo: '/login' },
];

@NgModule({
  imports: [RouterModule.forRoot(routes, { useHash: false })],
  exports: [RouterModule],
})
export class AppRoutingModule {}
