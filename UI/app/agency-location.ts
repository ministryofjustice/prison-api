import { Inmate } from './inmate';

export class AgencyLocation  {
	locationId: number;
	agencyId: string;
	locationType: string;
	description: string;
	parentLocationId?: number;
	operationalCapacity?: number;
	currentOccupancy: number;
	livingUnit: boolean;
	housingUnitType?: string;
	assignedInmates?: Inmate[];
}
