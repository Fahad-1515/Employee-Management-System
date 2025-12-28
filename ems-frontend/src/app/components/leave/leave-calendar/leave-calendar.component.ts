import { Component, OnInit } from '@angular/core';
import { CalendarOptions, EventClickArg } from '@fullcalendar/core';
import dayGridPlugin from '@fullcalendar/daygrid';
import timeGridPlugin from '@fullcalendar/timegrid';
import interactionPlugin from '@fullcalendar/interaction';
import { LeaveService } from '../../../services/leave.service';
import { LeaveRequest } from '../../../models/leave.model';

@Component({
  selector: 'app-leave-calendar',
  templateUrl: './leave-calendar.component.html',
  styleUrls: ['./leave-calendar.component.css'],
})
export class LeaveCalendarComponent implements OnInit {
  calendarOptions: CalendarOptions = {
    initialView: 'dayGridMonth',
    plugins: [dayGridPlugin, timeGridPlugin, interactionPlugin],
    headerToolbar: {
      left: 'prev,next today',
      center: 'title',
      right: 'dayGridMonth,timeGridWeek,timeGridDay',
    },
    weekends: true,
    editable: false,
    selectable: true,
    selectMirror: true,
    dayMaxEvents: true,
    events: [],
    eventClick: this.handleEventClick.bind(this),
    eventColor: '#3788d8',
    eventTextColor: '#ffffff',
    eventDisplay: 'block',
    height: 'auto',
  };

  leaveEvents: any[] = [];
  selectedDate: Date = new Date();
  loading = false;

  constructor(private leaveService: LeaveService) {}

  ngOnInit(): void {
    this.loadCalendarEvents();
  }

  loadCalendarEvents(): void {
    this.loading = true;
    const startDate = new Date(
      this.selectedDate.getFullYear(),
      this.selectedDate.getMonth(),
      1
    )
      .toISOString()
      .split('T')[0];

    const endDate = new Date(
      this.selectedDate.getFullYear(),
      this.selectedDate.getMonth() + 1,
      0
    )
      .toISOString()
      .split('T')[0];

    this.leaveService.getTeamCalendar(startDate, endDate).subscribe({
      next: (leaves) => {
        this.leaveEvents = leaves.map((leave) => ({
          id: leave.id?.toString(),
          title: `${leave.employeeName} - ${leave.leaveType}`,
          start: leave.startDate,
          end: leave.endDate,
          backgroundColor: this.getEventColor(leave.leaveType),
          borderColor: this.getEventColor(leave.leaveType),
          extendedProps: {
            employeeName: leave.employeeName,
            leaveType: leave.leaveType,
            reason: leave.reason,
            status: leave.status,
          },
        }));

        this.calendarOptions.events = this.leaveEvents;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading calendar events:', error);
        this.loading = false;
      },
    });
  }

  getEventColor(leaveType: string): string {
    switch (leaveType) {
      case 'VACATION':
        return '#4caf50'; // Green
      case 'SICK':
        return '#f44336'; // Red
      case 'PERSONAL':
        return '#ff9800'; // Orange
      case 'MATERNITY':
        return '#9c27b0'; // Purple
      case 'PATERNITY':
        return '#2196f3'; // Blue
      default:
        return '#757575'; // Gray
    }
  }

  handleEventClick(clickInfo: EventClickArg): void {
    const event = clickInfo.event;
    const extendedProps = event.extendedProps;

    alert(
      `Employee: ${extendedProps['employeeName']}\n` +
        `Leave Type: ${extendedProps['leaveType']}\n` +
        `Status: ${extendedProps['status']}\n` +
        `Reason: ${extendedProps['reason']}\n` +
        `From: ${event.start?.toLocaleDateString()} To: ${event.end?.toLocaleDateString()}`
    );
  }

  onMonthChange(event: any): void {
    this.selectedDate = new Date(event.start);
    this.loadCalendarEvents();
  }

  refreshCalendar(): void {
    this.loadCalendarEvents();
  }
}
