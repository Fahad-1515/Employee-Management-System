import { Component, Inject, OnInit, ChangeDetectorRef } from '@angular/core'; // ADD ChangeDetectorRef
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { EmployeeService } from '../../services/employee.service';
import {
  Employee,
  COUNTRY_CODES,
  extractCountryCode,
  formatPhoneNumber,
} from '../../models/employee.model';

@Component({
  selector: 'app-employee-form',
  templateUrl: './employee-form.component.html',
  styleUrls: ['./employee-form.component.css'],
})
export class EmployeeFormComponent implements OnInit {
  employeeForm: FormGroup;
  isEdit = false;
  loading = false;
  loadingData = true; // ADDED: Loading state for departments/positions
  departments: string[] = [];
  positions: string[] = [];
  countryCodes = COUNTRY_CODES;
  hidePhoneHint = false;
  employeeId?: number;

  constructor(
    private fb: FormBuilder,
    private employeeService: EmployeeService,
    private snackBar: MatSnackBar,
    private cdRef: ChangeDetectorRef, // ADDED: For change detection
    public dialogRef: MatDialogRef<EmployeeFormComponent>,
    @Inject(MAT_DIALOG_DATA) public data: Employee
  ) {
    this.isEdit = !!data;
    this.employeeForm = this.createForm();

    // ====== CRITICAL FIX: Initialize arrays immediately ======
    this.departments = this.getFallbackDepartments();
    this.positions = this.getFallbackPositions();
    // ========================================================

    console.log('✅ Arrays initialized immediately in constructor');
    console.log('Departments:', this.departments);
    console.log('Positions:', this.positions);

    if (this.isEdit && data.id) {
      this.employeeId = data.id;
    }
  }

  ngOnInit(): void {
    console.log('🎯 EmployeeFormComponent.ngOnInit() called');
    
    console.log('Current arrays:', {
      departments: this.departments.length,
      positions: this.positions.length
    });

    this.loadFormData();
    
    console.log('🔄 Calling loadFilters()...');
    this.loadFilters(); // This will update arrays with API data if available

    // REMOVE the setTimeout hack completely
    // It's causing timing issues
  }

  createForm(): FormGroup {
    return this.fb.group({
      firstName: [
        '',
        [
          Validators.required,
          Validators.minLength(2),
          Validators.maxLength(50),
        ],
      ],
      lastName: [
        '',
        [
          Validators.required,
          Validators.minLength(2),
          Validators.maxLength(50),
        ],
      ],
      email: ['', [Validators.required, Validators.email]],
      countryCode: ['+1', Validators.required],
      phoneNumber: [
        '',
        [Validators.required, Validators.pattern(/^[0-9]{8,15}$/)],
      ],
      department: ['', Validators.required],
      position: ['', Validators.required],
      salary: ['', [Validators.required, Validators.min(0)]],
    });
  }

  loadFormData(): void {
    if (this.isEdit && this.data) {
      console.log('Loading edit data:', this.data);

      let countryCode = '+1';
      let phoneNumber = this.data.phoneNumber || '';

      if (phoneNumber && phoneNumber.startsWith('+')) {
        // Find matching country code
        const country = this.countryCodes.find((code) =>
          phoneNumber.startsWith(code.code)
        );
        if (country) {
          countryCode = country.code;
          phoneNumber = phoneNumber.substring(country.code.length);
        }
      }

      // Handle salary conversion (ensure it's a number)
      const salary =
        typeof this.data.salary === 'string'
          ? parseFloat(this.data.salary)
          : this.data.salary || 0;

      this.employeeForm.patchValue({
        firstName: this.data.firstName || '',
        lastName: this.data.lastName || '',
        email: this.data.email || '',
        countryCode: countryCode,
        phoneNumber: phoneNumber,
        department: this.data.department || '',
        position: this.data.position || '',
        salary: salary,
      });
    }
  }

  loadFilters(): void {
    console.log('🔍 loadFilters() called');
    this.loadingData = true; // Start loading
    
    let departmentsLoaded = false;
    let positionsLoaded = false;

    const checkAllLoaded = () => {
      if (departmentsLoaded && positionsLoaded) {
        this.loadingData = false;
        this.cdRef.detectChanges(); // Force UI update
        console.log('✅ All filters loaded');
      }
    };

    // Load departments
    this.employeeService.getDepartments().subscribe({
      next: (departments) => {
        console.log('✅ Departments API response:', departments);
        
        if (departments && departments.length > 0) {
          this.departments = departments;
          console.log('📋 Departments updated from API:', this.departments);
        } else {
          // Keep fallback values
          console.log('⚠️ API returned empty, keeping fallback departments');
        }
        departmentsLoaded = true;
        checkAllLoaded();
      },
      error: (error) => {
        console.error('❌ Error fetching departments:', error);
        console.error('Error status:', error.status);
        console.error('Error message:', error.message);
        
        // Keep fallback values (already set in constructor)
        console.log('🔄 Using fallback departments due to error');
        departmentsLoaded = true;
        checkAllLoaded();
      },
    });

    // Load positions
    this.employeeService.getPositions().subscribe({
      next: (positions) => {
        console.log('✅ Positions API response:', positions);
        
        if (positions && positions.length > 0) {
          this.positions = positions;
          console.log('📋 Positions updated from API:', this.positions);
        } else {
          // Keep fallback values
          console.log('⚠️ API returned empty, keeping fallback positions');
        }
        positionsLoaded = true;
        checkAllLoaded();
      },
      error: (error) => {
        console.error('❌ Error fetching positions:', error);
        console.error('Error status:', error.status);
        console.error('Error message:', error.message);
        
        // Keep fallback values (already set in constructor)
        console.log('🔄 Using fallback positions due to error');
        positionsLoaded = true;
        checkAllLoaded();
      },
    });
  }

  getFallbackDepartments(): string[] {
    return [
      'IT',
      'HR',
      'Finance',
      'Marketing',
      'Sales',
      'Operations',
      'R&D',
      'Support',
    ];
  }

  getFallbackPositions(): string[] {
    return [
      'Software Engineer',
      'HR Manager',
      'Financial Analyst',
      'Marketing Specialist',
      'Sales Manager',
      'Operations Manager',
      'System Administrator',
      'Frontend Developer',
      'Backend Developer',
      'UI/UX Designer',
      'Data Analyst',
      'Project Manager',
    ];
  }

  onSubmit(): void {
    console.log('Form submitted. Valid:', this.employeeForm.valid);

    if (this.employeeForm.valid) {
      this.loading = true;

      // Get form values
      const formData = this.employeeForm.value;
      console.log('Form data:', formData);

      // Create employee object
      const employeeData: Employee = {
        firstName: formData.firstName,
        lastName: formData.lastName,
        email: formData.email,
        phoneNumber: `${formData.countryCode}${formData.phoneNumber}`,
        countryCode: formData.countryCode,
        department: formData.department,
        position: formData.position,
        salary: parseFloat(formData.salary) || 0,
      };

      console.log('Sending employee data:', employeeData);

      // Determine operation
      let operation;
      if (this.isEdit && this.employeeId) {
        console.log('Updating employee ID:', this.employeeId);
        operation = this.employeeService.updateEmployee(
          this.employeeId,
          employeeData
        );
      } else {
        console.log('Creating new employee');
        operation = this.employeeService.createEmployee(employeeData);
      }

      // Execute operation
      operation.subscribe({
        next: (response) => {
          console.log('Employee operation successful:', response);
          this.loading = false;
          this.snackBar.open(
            `Employee ${this.isEdit ? 'updated' : 'created'} successfully!`,
            'Close',
            { duration: 3000 }
          );
          this.dialogRef.close(true); // Pass true to indicate success
        },
        error: (error) => {
          console.error('Employee operation error:', error);
          this.loading = false;

          let errorMessage = `Error ${
            this.isEdit ? 'updating' : 'creating'
          } employee`;
          if (error.error) {
            if (typeof error.error === 'string') {
              errorMessage += `: ${error.error}`;
            } else if (error.error.message) {
              errorMessage += `: ${error.error.message}`;
            } else if (error.error.error) {
              errorMessage += `: ${error.error.error}`;
            }
          } else if (error.message) {
            errorMessage += `: ${error.message}`;
          }

          this.snackBar.open(errorMessage, 'Close', {
            duration: 5000,
            panelClass: ['error-snackbar'],
          });
        },
        complete: () => {
          console.log('Employee operation completed');
        },
      });
    } else {
      console.log('Form invalid, marking as touched');
      this.markFormGroupTouched();

      // Show validation errors
      this.snackBar.open('Please fill all required fields correctly', 'Close', {
        duration: 3000,
        panelClass: ['error-snackbar'],
      });
    }
  }

  onCancel(): void {
    console.log('Form cancelled');
    this.dialogRef.close(false);
  }

  private markFormGroupTouched(): void {
    Object.keys(this.employeeForm.controls).forEach((key) => {
      const control = this.employeeForm.get(key);
      control?.markAsTouched();
      control?.updateValueAndValidity();
    });
  }

  getFullPhoneNumber(): string {
    const countryCode = this.employeeForm.get('countryCode')?.value;
    const phoneNumber = this.employeeForm.get('phoneNumber')?.value;

    if (countryCode && phoneNumber) {
      return formatPhoneNumber(`${countryCode}${phoneNumber}`);
    }

    return '';
  }

  updatePhoneHint(): void {
    const countryCode = this.employeeForm.get('countryCode')?.value;
    const phoneNumber = this.employeeForm.get('phoneNumber')?.value;
    this.hidePhoneHint = !countryCode || !phoneNumber;
  }

  getSelectedCountryName(): string {
    const countryCode = this.employeeForm.get('countryCode')?.value;
    const country = this.countryCodes.find((c) => c.code === countryCode);
    return country ? `${country.flag} ${country.name}` : '';
  }

  get title(): string {
    return this.isEdit ? 'Edit Employee' : 'Add New Employee';
  }

  get buttonText(): string {
    return this.isEdit ? 'Update Employee' : 'Create Employee';
  }

  // Validation helpers for template
  get firstName() {
    return this.employeeForm.get('firstName');
  }
  get lastName() {
    return this.employeeForm.get('lastName');
  }
  get email() {
    return this.employeeForm.get('email');
  }
  get countryCode() {
    return this.employeeForm.get('countryCode');
  }
  get phoneNumber() {
    return this.employeeForm.get('phoneNumber');
  }
  get department() {
    return this.employeeForm.get('department');
  }
  get position() {
    return this.employeeForm.get('position');
  }
  get salary() {
    return this.employeeForm.get('salary');
  }
}
