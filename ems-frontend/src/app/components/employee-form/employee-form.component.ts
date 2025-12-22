import { Component, Inject, OnInit, ChangeDetectorRef, OnDestroy } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { EmployeeService } from '../../services/employee.service';
import { AuthService } from '../../services/auth.service';
import { Employee, COUNTRY_CODES, formatPhoneNumber } from '../../models/employee.model';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-employee-form',
  templateUrl: './employee-form.component.html',
  styleUrls: ['./employee-form.component.css'],
})
export class EmployeeFormComponent implements OnInit, OnDestroy {
  employeeForm: FormGroup;
  isEdit = false;
  loading = false;
  loadingData = false;
  departments: string[] = [];
  positions: string[] = [];
  countryCodes = COUNTRY_CODES;
  hidePhoneHint = false;
  employeeId?: number;
  
  // Track loading state properly
  private departmentsLoaded = false;
  private positionsLoaded = false;
  private subscriptions: Subscription[] = [];

  // Form field getters
  get firstName() { return this.employeeForm.get('firstName'); }
  get lastName() { return this.employeeForm.get('lastName'); }
  get email() { return this.employeeForm.get('email'); }
  get countryCode() { return this.employeeForm.get('countryCode'); }
  get phoneNumber() { return this.employeeForm.get('phoneNumber'); }
  get department() { return this.employeeForm.get('department'); }
  get position() { return this.employeeForm.get('position'); }
  get salary() { return this.employeeForm.get('salary'); }

  get title(): string {
    return this.isEdit ? 'Edit Employee' : 'Add New Employee';
  }

  get buttonText(): string {
    return this.isEdit ? 'Update Employee' : 'Create Employee';
  }

  constructor(
    private fb: FormBuilder,
    private employeeService: EmployeeService,
    private authService: AuthService,
    private snackBar: MatSnackBar,
    private cdRef: ChangeDetectorRef,
    public dialogRef: MatDialogRef<EmployeeFormComponent>,
    @Inject(MAT_DIALOG_DATA) public data: Employee | null
  ) {
    console.log('🎯 EmployeeFormComponent initialized');
    console.log('Mode:', data ? 'Edit' : 'Create');
    
    this.isEdit = !!data;
    this.employeeForm = this.createForm();
    
    // Initialize with fallback data immediately
    this.departments = this.getFallbackDepartments();
    this.positions = this.getFallbackPositions();
    console.log('📊 Initial departments:', this.departments);
    console.log('📊 Initial positions:', this.positions);
    
    if (this.isEdit && data?.id) {
      this.employeeId = data.id;
    }
  }

  ngOnInit(): void {
    console.log('🎯 EmployeeFormComponent.ngOnInit()');
    
    this.loadFormData();
    this.loadFilters();
    
    // Auto-update phone hint when fields change
    this.subscriptions.push(
      this.employeeForm.get('countryCode')?.valueChanges.subscribe(() => {
        this.updatePhoneHint();
      }) || new Subscription()
    );
    
    this.subscriptions.push(
      this.employeeForm.get('phoneNumber')?.valueChanges.subscribe(() => {
        this.updatePhoneHint();
      }) || new Subscription()
    );
  }

  ngOnDestroy(): void {
    // Clean up subscriptions
    this.subscriptions.forEach(sub => sub.unsubscribe());
  }

  createForm(): FormGroup {
    return this.fb.group({
      firstName: ['', [
        Validators.required,
        Validators.minLength(2),
        Validators.maxLength(50),
        Validators.pattern(/^[a-zA-Z\s]*$/)
      ]],
      lastName: ['', [
        Validators.required,
        Validators.minLength(2),
        Validators.maxLength(50),
        Validators.pattern(/^[a-zA-Z\s]*$/)
      ]],
      email: ['', [
        Validators.required,
        Validators.email,
        Validators.pattern(/^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/)
      ]],
      countryCode: ['+1', [Validators.required]],
      phoneNumber: ['', [
        Validators.required,
        Validators.pattern(/^[0-9]{8,15}$/),
        Validators.minLength(8),
        Validators.maxLength(15)
      ]],
      department: ['', [Validators.required]],
      position: ['', [Validators.required]],
      salary: ['', [
        Validators.required,
        Validators.min(0),
        Validators.max(1000000),
        Validators.pattern(/^\d+(\.\d{1,2})?$/)
      ]]
    });
  }

  loadFormData(): void {
    if (this.isEdit && this.data) {
      console.log('📝 Loading edit data:', this.data);
      
      let countryCode = '+1';
      let phoneNumber = this.data.phoneNumber || '';
      
      // Extract country code from full phone number
      if (phoneNumber.startsWith('+')) {
        const country = this.countryCodes.find(c => 
          phoneNumber.startsWith(c.code)
        );
        if (country) {
          countryCode = country.code;
          phoneNumber = phoneNumber.substring(country.code.length);
        }
      }
      
      this.employeeForm.patchValue({
        firstName: this.data.firstName || '',
        lastName: this.data.lastName || '',
        email: this.data.email || '',
        countryCode: countryCode,
        phoneNumber: phoneNumber,
        department: this.data.department || '',
        position: this.data.position || '',
        salary: this.data.salary || 0
      });
    }
  }

  loadFilters(): void {
    console.log('🔄 Starting loadFilters()');
    console.log('Current departments:', this.departments.length);
    console.log('Current positions:', this.positions.length);
    
    this.loadingData = true;
    
    // Load departments with detailed logging
    console.log('📡 Calling employeeService.getDepartments()');
    const deptSub = this.employeeService.getDepartments().subscribe({
      next: (departments) => {
        console.log('📦 Departments API SUCCESS!');
        console.log('Received data:', departments);
        console.log('Is Array?', Array.isArray(departments));
        console.log('Length:', departments?.length);
        console.log('First item:', departments?.[0]);
        console.log('Full array:', departments);
        
        if (departments && Array.isArray(departments) && departments.length > 0) {
          this.departments = departments;
          console.log(`✅ Loaded ${departments.length} departments from API`);
        } else {
          console.warn('⚠️ API returned empty/not array, keeping fallback');
          console.log('Current departments remain:', this.departments);
        }
        this.departmentsLoaded = true;
        this.checkDataLoaded();
      },
      error: (error) => {
        console.error('💥 Departments API ERROR:');
        console.error('Error object:', error);
        console.error('Status:', error?.status);
        console.error('Message:', error?.message);
        console.error('URL:', error?.url);
        
        console.log('Keeping fallback departments:', this.departments);
        this.departmentsLoaded = true;
        this.checkDataLoaded();
      }
    });
    
    // Load positions with detailed logging
    console.log('📡 Calling employeeService.getPositions()');
    const posSub = this.employeeService.getPositions().subscribe({
      next: (positions) => {
        console.log('📦 Positions API SUCCESS!');
        console.log('Received data:', positions);
        console.log('Is Array?', Array.isArray(positions));
        console.log('Length:', positions?.length);
        console.log('First item:', positions?.[0]);
        
        if (positions && Array.isArray(positions) && positions.length > 0) {
          this.positions = positions;
          console.log(`✅ Loaded ${positions.length} positions from API`);
        } else {
          console.warn('⚠️ API returned empty positions, keeping fallback');
          console.log('Current positions remain:', this.positions);
        }
        this.positionsLoaded = true;
        this.checkDataLoaded();
      },
      error: (error) => {
        console.error('💥 Positions API ERROR:');
        console.error('Error object:', error);
        console.error('Status:', error?.status);
        console.error('Message:', error?.message);
        
        console.log('Keeping fallback positions:', this.positions);
        this.positionsLoaded = true;
        this.checkDataLoaded();
      }
    });
    
    this.subscriptions.push(deptSub, posSub);
    
    // Set timeout to show loading state
    setTimeout(() => {
      if (!this.departmentsLoaded || !this.positionsLoaded) {
        console.log('⏳ Still loading after 2 seconds...');
        console.log('Departments loaded?', this.departmentsLoaded);
        console.log('Positions loaded?', this.positionsLoaded);
      }
    }, 2000);
  }

  private checkDataLoaded(): void {
    console.log('🔄 checkDataLoaded() called');
    console.log('Departments loaded?', this.departmentsLoaded);
    console.log('Positions loaded?', this.positionsLoaded);
    console.log('Departments count:', this.departments.length);
    console.log('Positions count:', this.positions.length);
    console.log('Departments:', this.departments);
    console.log('Positions:', this.positions);
    
    if (this.departmentsLoaded && this.positionsLoaded) {
      console.log('✅✅✅ BOTH DEPARTMENTS AND POSITIONS LOADED! ✅✅✅');
      console.log('Final departments:', this.departments);
      console.log('Final positions:', this.positions);
      
      this.loadingData = false;
      
      // Force UI update
      this.cdRef.detectChanges();
      
      // Additional check after UI update
      setTimeout(() => {
        console.log('🔄 Final check - Form values:');
        console.log('Department control value:', this.department?.value);
        console.log('Position control value:', this.position?.value);
        console.log('Department control valid?', this.department?.valid);
        console.log('Position control valid?', this.position?.valid);
      }, 100);
    } else {
      console.log('⏳ Waiting for other data...');
      console.log('Need departments:', !this.departmentsLoaded);
      console.log('Need positions:', !this.positionsLoaded);
    }
  }

  onSubmit(): void {
    if (this.employeeForm.invalid) {
      console.log('❌ Form invalid!');
      console.log('Form errors:', this.employeeForm.errors);
      console.log('Department errors:', this.department?.errors);
      console.log('Position errors:', this.position?.errors);
      console.log('Department value:', this.department?.value);
      console.log('Position value:', this.position?.value);
      
      this.markAllFieldsAsTouched();
      this.showErrorMessage('Please fill all required fields correctly');
      return;
    }

    this.loading = true;
    
    const formValue = this.employeeForm.value;
    const employeeData: Employee = {
      firstName: formValue.firstName.trim(),
      lastName: formValue.lastName.trim(),
      email: formValue.email.toLowerCase().trim(),
      phoneNumber: `${formValue.countryCode}${formValue.phoneNumber}`,
      countryCode: formValue.countryCode,
      department: formValue.department,
      position: formValue.position,
      salary: parseFloat(formValue.salary)
    };

    console.log('📤 Submitting employee data:', employeeData);

    const operation = this.isEdit && this.employeeId
      ? this.employeeService.updateEmployee(this.employeeId, employeeData)
      : this.employeeService.createEmployee(employeeData);

    const sub = operation.subscribe({
      next: (response) => {
        console.log('✅ Employee saved successfully:', response);
        this.showSuccessMessage(
          `Employee ${this.isEdit ? 'updated' : 'created'} successfully!`
        );
        this.dialogRef.close({ success: true, data: response });
      },
      error: (error) => {
        console.error('❌ Save operation failed:', error);
        this.handleSaveError(error);
        this.loading = false;
      },
      complete: () => {
        this.loading = false;
      }
    });

    this.subscriptions.push(sub);
  }

  onCancel(): void {
    console.log('Form cancelled');
    this.dialogRef.close({ success: false });
  }

  getFullPhoneNumber(): string {
    const countryCode = this.countryCode?.value;
    const phoneNumber = this.phoneNumber?.value;
    
    if (countryCode && phoneNumber) {
      return formatPhoneNumber(`${countryCode}${phoneNumber}`);
    }
    return '';
  }

  updatePhoneHint(): void {
    this.hidePhoneHint = !this.countryCode?.value || !this.phoneNumber?.value;
  }

  getSelectedCountryName(): string {
    const countryCode = this.countryCode?.value;
    const country = this.countryCodes.find(c => c.code === countryCode);
    return country ? `${country.flag} ${country.name}` : 'Select country';
  }

  // Helper Methods
  private markAllFieldsAsTouched(): void {
    Object.values(this.employeeForm.controls).forEach(control => {
      control.markAsTouched();
      control.updateValueAndValidity();
    });
  }

  private showSuccessMessage(message: string): void {
    this.snackBar.open(message, 'Close', {
      duration: 3000,
      panelClass: ['success-snackbar']
    });
  }

  private showErrorMessage(message: string): void {
    this.snackBar.open(message, 'Close', {
      duration: 5000,
      panelClass: ['error-snackbar']
    });
  }

  private handleSaveError(error: any): void {
    let errorMessage = 'Failed to save employee';
    
    if (error.status === 400) {
      errorMessage = 'Validation error. Please check your input.';
    } else if (error.status === 409) {
      errorMessage = 'Email already exists. Please use a different email.';
    } else if (error.status === 401) {
      errorMessage = 'Session expired. Please login again.';
      this.authService.logout();
    } else if (error.error?.message) {
      errorMessage = error.error.message;
    } else if (error.message) {
      errorMessage = error.message;
    }
    
    this.showErrorMessage(errorMessage);
  }

  // Fallback Data - Match DataInitializer from backend
  private getFallbackDepartments(): string[] {
    return [
      'IT', 'HR', 'Finance', 'Marketing', 
      'Sales', 'Operations', 'Design', 'Support'
    ];
  }

  private getFallbackPositions(): string[] {
    return [
      'Software Engineer', 'HR Manager', 'Financial Analyst',
      'Marketing Specialist', 'Sales Manager', 'System Administrator',
      'Operations Manager', 'Frontend Developer', 'Backend Developer',
      'UI/UX Designer', 'Accountant', 'Recruiter', 'Sales Executive'
    ];
  }
  
  // Test method to check backend directly
  testBackendConnection(): void {
    console.log('🧪 Testing backend connection directly...');
    
    // Test departments endpoint
    fetch('https://employee-management-system-jxdj.onrender.com/api/employees/departments')
      .then(response => {
        console.log('📡 Departments Response:');
        console.log('  Status:', response.status);
        console.log('  OK:', response.ok);
        console.log('  Headers:', response.headers);
        return response.json();
      })
      .then(data => {
        console.log('🎉 Departments Data:', data);
        console.log('  Type:', typeof data);
        console.log('  Length:', data?.length);
        console.log('  Full:', JSON.stringify(data));
      })
      .catch(error => {
        console.error('💥 Departments Fetch Error:', error);
      });
    
    // Test positions endpoint
    fetch('https://employee-management-system-jxdj.onrender.com/api/employees/positions')
      .then(response => {
        console.log('📡 Positions Response:');
        console.log('  Status:', response.status);
        console.log('  OK:', response.ok);
        return response.json();
      })
      .then(data => {
        console.log('🎉 Positions Data:', data);
        console.log('  Type:', typeof data);
        console.log('  Length:', data?.length);
      })
      .catch(error => {
        console.error('💥 Positions Fetch Error:', error);
      });
  }
}
