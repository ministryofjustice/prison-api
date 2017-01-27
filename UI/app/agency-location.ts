import { Inmate } from './inmate';

export class AgencyLocation  {
	id: number;
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
