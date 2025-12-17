// src/app/components/analytics/analytics.component.ts
import { Component, OnInit, ViewChild } from '@angular/core';
import { EmployeeService } from '../../services/employee.service';
import { Employee } from '../../models/employee.model';
import { BaseChartDirective } from 'ng2-charts';

@Component({
  selector: 'app-analytics',
  templateUrl: './analytics.component.html',
  styleUrls: ['./analytics.component.css'],
})
export class AnalyticsComponent implements OnInit {
  @ViewChild(BaseChartDirective) chart?: BaseChartDirective;

  loading = false;
  employees: Employee[] = [];

  // Analytics Data
  analytics = {
    totalEmployees: 0,
    totalDepartments: 0,
    totalSalary: 0,
    averageSalary: 0,
    departmentStats: [] as any[],
    positionStats: [] as any[],
    salaryDistribution: [] as any[],
    recentHires: 0,
  };

  // Chart Data
  departmentChartData: any;
  salaryChartData: any;
  positionChartData: any;

  // Chart Options (Updated for Chart.js v4)
  chartOptions = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        position: 'bottom' as const,
      },
      tooltip: {
        callbacks: {
          label: (context: any) => {
            const label = context.label || '';
            const value = context.parsed || context.formattedValue || 0;
            return `${label}: ${value} employees`;
          },
        },
      },
    },
  };

  salaryChartOptions = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        display: false,
      },
      tooltip: {
        callbacks: {
          label: (context: any) => {
            return `$${context.parsed.y?.toLocaleString() || '0'}`;
          },
        },
      },
    },
    scales: {
      y: {
        beginAtZero: true,
        ticks: {
          callback: (value: any) => {
            return '$' + value?.toLocaleString() || '0';
          },
        },
      },
    },
  };

  constructor(private employeeService: EmployeeService) {}

  ngOnInit(): void {
    this.loadAnalytics();
  }

  loadAnalytics(): void {
    this.loading = true;

    this.employeeService.searchEmployees(0, 1000).subscribe({
      next: (response) => {
        this.employees = response.content || [];
        this.calculateAnalytics();
        this.prepareCharts();
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading analytics:', error);
        this.loading = false;
        // Initialize with empty data to prevent template errors
        this.initializeEmptyCharts();
      },
    });
  }

  calculateAnalytics(): void {
    const employees = this.employees;

    if (!employees || employees.length === 0) {
      this.initializeEmptyAnalytics();
      return;
    }

    // Basic stats
    this.analytics.totalEmployees = employees.length;
    this.analytics.totalSalary = employees.reduce(
      (sum, emp) => sum + (emp.salary || 0),
      0
    );
    this.analytics.averageSalary =
      employees.length > 0 ? this.analytics.totalSalary / employees.length : 0;

    // Department statistics
    const departmentMap = new Map();
    const positionMap = new Map();

    employees.forEach((employee) => {
      if (!employee.department) return;

      // Department stats
      if (departmentMap.has(employee.department)) {
        departmentMap.set(
          employee.department,
          departmentMap.get(employee.department) + 1
        );
      } else {
        departmentMap.set(employee.department, 1);
      }

      // Position stats
      if (employee.position) {
        if (positionMap.has(employee.position)) {
          positionMap.set(
            employee.position,
            positionMap.get(employee.position) + 1
          );
        } else {
          positionMap.set(employee.position, 1);
        }
      }
    });

    this.analytics.departmentStats = Array.from(departmentMap.entries()).map(
      ([name, count]) => ({
        name,
        count,
        percentage: (count / employees.length) * 100,
      })
    );

    this.analytics.positionStats = Array.from(positionMap.entries()).map(
      ([name, count]) => ({
        name,
        count,
        percentage: (count / employees.length) * 100,
      })
    );

    this.analytics.totalDepartments = departmentMap.size;

    // Salary distribution
    const salaryRanges = [
      { range: '0-50k', min: 0, max: 50000, count: 0 },
      { range: '50k-75k', min: 50000, max: 75000, count: 0 },
      { range: '75k-100k', min: 75000, max: 100000, count: 0 },
      { range: '100k+', min: 100000, max: Infinity, count: 0 },
    ];

    employees.forEach((employee) => {
      const salary = employee.salary || 0;
      const range = salaryRanges.find((r) => salary >= r.min && salary < r.max);
      if (range) range.count++;
    });

    this.analytics.salaryDistribution = salaryRanges;

    // Recent hires (last 30 days - mock data for demo)
    this.analytics.recentHires = Math.floor(Math.random() * 5) + 1;
  }

  prepareCharts(): void {
    // Department Chart
    this.departmentChartData = {
      labels: this.analytics.departmentStats.map((dept) => dept.name),
      datasets: [
        {
          data: this.analytics.departmentStats.map((dept) => dept.count),
          backgroundColor: [
            '#FF6384',
            '#36A2EB',
            '#FFCE56',
            '#4BC0C0',
            '#9966FF',
            '#FF9F40',
            '#8AC926',
            '#C9CBCF',
          ],
          hoverBackgroundColor: [
            '#FF6384',
            '#36A2EB',
            '#FFCE56',
            '#4BC0C0',
            '#9966FF',
            '#FF9F40',
            '#8AC926',
            '#C9CBCF',
          ],
        },
      ],
    };

    // Salary Chart (Bar chart for average salary by department)
    const departmentSalaries = this.analytics.departmentStats.map((dept) => {
      const deptEmployees = this.employees.filter(
        (emp) => emp.department === dept.name
      );
      if (deptEmployees.length === 0) return 0;
      return (
        deptEmployees.reduce((sum, emp) => sum + (emp.salary || 0), 0) /
        deptEmployees.length
      );
    });

    this.salaryChartData = {
      labels: this.analytics.departmentStats.map((dept) => dept.name),
      datasets: [
        {
          label: 'Average Salary',
          data: departmentSalaries,
          backgroundColor: '#36A2EB',
          borderColor: '#36A2EB',
          borderWidth: 1,
        },
      ],
    };

    // Position Chart
    const topPositions = this.analytics.positionStats
      .sort((a, b) => b.count - a.count)
      .slice(0, 8);

    this.positionChartData = {
      labels: topPositions.map((pos) => pos.name),
      datasets: [
        {
          label: 'Employees by Position',
          data: topPositions.map((pos) => pos.count),
          backgroundColor: '#4BC0C0',
          borderColor: '#4BC0C0',
          borderWidth: 1,
        },
      ],
    };

    // Refresh charts if they exist
    if (this.chart) {
      this.chart.update();
    }
  }

  exportAnalyticsReport(): void {
    this.employeeService.exportToExcel().subscribe({
      next: (blob: Blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `analytics_report_${new Date().getTime()}.xlsx`;
        a.click();
        window.URL.revokeObjectURL(url);
      },
      error: (error) => {
        console.error('Error exporting analytics:', error);
      },
    });
  }

  refreshAnalytics(): void {
    this.loadAnalytics();
  }

  getChartColor(department: string): string {
    const colors = [
      '#FF6384',
      '#36A2EB',
      '#FFCE56',
      '#4BC0C0',
      '#9966FF',
      '#FF9F40',
      '#8AC926',
      '#C9CBCF',
    ];
    const index = this.analytics.departmentStats.findIndex(
      (dept) => dept.name === department
    );
    return colors[index % colors.length] || '#C9CBCF';
  }

  getDepartmentAverageSalary(department: string): number {
    const deptEmployees = this.employees.filter(
      (emp) => emp.department === department
    );
    if (deptEmployees.length === 0) return 0;
    return (
      deptEmployees.reduce((sum, emp) => sum + (emp.salary || 0), 0) /
      deptEmployees.length
    );
  }

  // Initialize empty charts when no data is available
  private initializeEmptyCharts(): void {
    this.departmentChartData = {
      labels: ['No Data'],
      datasets: [
        {
          data: [1],
          backgroundColor: ['#C9CBCF'],
        },
      ],
    };

    this.salaryChartData = {
      labels: ['No Data'],
      datasets: [
        {
          label: 'Average Salary',
          data: [0],
          backgroundColor: '#C9CBCF',
        },
      ],
    };

    this.positionChartData = {
      labels: ['No Data'],
      datasets: [
        {
          label: 'Employees by Position',
          data: [0],
          backgroundColor: '#C9CBCF',
        },
      ],
    };
  }

  private initializeEmptyAnalytics(): void {
    this.analytics = {
      totalEmployees: 0,
      totalDepartments: 0,
      totalSalary: 0,
      averageSalary: 0,
      departmentStats: [],
      positionStats: [],
      salaryDistribution: [
        { range: '0-50k', min: 0, max: 50000, count: 0 },
        { range: '50k-75k', min: 50000, max: 75000, count: 0 },
        { range: '75k-100k', min: 75000, max: 100000, count: 0 },
        { range: '100k+', min: 100000, max: Infinity, count: 0 },
      ],
      recentHires: 0,
    };
  }
}
