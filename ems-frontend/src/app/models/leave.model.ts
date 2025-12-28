export interface LeaveRequest {
  id?: number;
  employeeId: number;
  employeeName?: string;
  leaveType:
    | 'VACATION'
    | 'SICK'
    | 'PERSONAL'
    | 'MATERNITY'
    | 'PATERNITY'
    | 'UNPAID';
  startDate: string;
  endDate: string;
  totalDays: number;
  reason: string;
  status: 'PENDING' | 'APPROVED' | 'REJECTED' | 'CANCELLED';
  approvedBy?: string;
  approvedDate?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface LeaveBalance {
  employeeId: number;
  vacationDays: number;
  sickDays: number;
  personalDays: number;
  maternityDays: number;
  paternityDays: number;
  unpaidDays: number;
  usedVacation: number;
  usedSick: number;
  usedPersonal: number;
}

export interface LeavePolicy {
  id?: number;
  vacationDays: number;
  sickDays: number;
  personalDays: number;
  maternityDays: number;
  paternityDays: number;
  maxConsecutiveDays: number;
  advanceNoticeDays: number;
  carryOverEnabled: boolean;
  maxCarryOverDays: number;
}

export interface LeaveStats {
  pendingRequests: number;
  approvedThisMonth: number;
  rejectedThisMonth: number;
  totalLeavesTaken: number;
  averageLeaveDuration: number;
}
