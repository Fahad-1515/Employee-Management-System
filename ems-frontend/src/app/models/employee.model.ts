export interface Employee {
  id?: number;
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber: string;
  countryCode: string; // Added country code
  department: string;
  position: string;
  salary: number;
  hireDate?: string; // Added hire date
  createdAt?: string;
  updatedAt?: string;
}

export interface EmployeeResponse {
  content: Employee[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export interface EmployeeSearchCriteria {
  department?: string;
  position?: string;
  minSalary?: number;
  maxSalary?: number;
  searchTerm?: string;
}

// Country codes constant for dropdown
export const COUNTRY_CODES = [
  { code: '+1', name: 'USA/Canada', flag: '🇺🇸' },
  { code: '+44', name: 'UK', flag: '🇬🇧' },
  { code: '+91', name: 'India', flag: '🇮🇳' },
  { code: '+61', name: 'Australia', flag: '🇦🇺' },
  { code: '+49', name: 'Germany', flag: '🇩🇪' },
  { code: '+33', name: 'France', flag: '🇫🇷' },
  { code: '+81', name: 'Japan', flag: '🇯🇵' },
  { code: '+86', name: 'China', flag: '🇨🇳' },
  { code: '+7', name: 'Russia', flag: '🇷🇺' },
  { code: '+55', name: 'Brazil', flag: '🇧🇷' },
  { code: '+34', name: 'Spain', flag: '🇪🇸' },
  { code: '+39', name: 'Italy', flag: '🇮🇹' },
  { code: '+82', name: 'South Korea', flag: '🇰🇷' },
  { code: '+52', name: 'Mexico', flag: '🇲🇽' },
  { code: '+27', name: 'South Africa', flag: '🇿🇦' },
  { code: '+971', name: 'UAE', flag: '🇦🇪' },
  { code: '+65', name: 'Singapore', flag: '🇸🇬' },
  { code: '+31', name: 'Netherlands', flag: '🇳🇱' },
  { code: '+46', name: 'Sweden', flag: '🇸🇪' },
  { code: '+41', name: 'Switzerland', flag: '🇨🇭' },
];

// Helper function to format phone numbers for display
export function formatPhoneNumber(
  phoneNumber: string,
  countryCode?: string
): string {
  if (!phoneNumber) return '';

  // If phone number already has country code
  if (phoneNumber.startsWith('+')) {
    const number = phoneNumber.substring(1);

    // Format US/Canada numbers: +1 (234) 567-8900
    if (phoneNumber.startsWith('+1') && number.length === 11) {
      return `+1 (${number.substring(1, 4)}) ${number.substring(
        4,
        7
      )}-${number.substring(7)}`;
    }

    // Format UK numbers: +44 7123 456789
    if (phoneNumber.startsWith('+44') && number.length === 12) {
      return `+44 ${number.substring(2, 6)} ${number.substring(6)}`;
    }

    return phoneNumber;
  }

  // If we have separate country code and number
  if (countryCode) {
    return `${countryCode}${phoneNumber}`;
  }

  return phoneNumber;
}

// Helper function to extract country code from phone number
export function extractCountryCode(phoneNumber: string): {
  countryCode: string;
  number: string;
} {
  if (!phoneNumber) return { countryCode: '+1', number: '' };

  if (phoneNumber.startsWith('+')) {
    // Common country code patterns
    const patterns = [
      { code: '+1', length: 12 }, // USA/Canada
      { code: '+44', length: 13 }, // UK
      { code: '+91', length: 13 }, // India
      { code: '+61', length: 12 }, // Australia
      { code: '+49', length: 13 }, // Germany
      { code: '+33', length: 12 }, // France
      { code: '+81', length: 12 }, // Japan
      { code: '+86', length: 13 }, // China
      { code: '+7', length: 12 }, // Russia
      { code: '+55', length: 13 }, // Brazil
    ];

    for (const pattern of patterns) {
      if (
        phoneNumber.startsWith(pattern.code) &&
        phoneNumber.length === pattern.length
      ) {
        return {
          countryCode: pattern.code,
          number: phoneNumber.substring(pattern.code.length),
        };
      }
    }

    // Default: extract first 1-4 digits after + as country code
    const match = phoneNumber.match(/^(\+\d{1,4})(\d+)$/);
    if (match) {
      return {
        countryCode: match[1],
        number: match[2],
      };
    }
  }

  return { countryCode: '+1', number: phoneNumber };
}

// Interface for country code selection
export interface CountryCode {
  code: string;
  name: string;
  flag: string;
}
