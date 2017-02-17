export class PhysicalAttributes {
  gender: string;
  ethnicity: string;
  heightInches: number;
  heightMeters: number;
  weightPounds: number;
  weightKg: number;
}

export class PhysicalCharacteristic {
  characteristic: string;
  detail: string;
  imageId?: number;
}

export class PhysicalMark {
  type: string;
  side: string;
  bodyPart: string;
  orientation: string;
  comment: string;
  imageId?: number;
}

export class Inmate {
  id: number;
  bookingId: string;
  offenderId: string;
  firstName: string;
  lastName: string;

  middleName?: string;
  alertCodes?: string[];
  currentLocationId?: number;
  assignedLivingUnitId?: number;
  facialImageId?: number;
  dateOfBirth?: string;
  age?: number;
  physicalAttributes?: PhysicalAttributes
  physicalCharacteristics?: PhysicalCharacteristic[]
  physicalMarks?: PhysicalMark[]
}
