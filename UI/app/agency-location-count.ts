
export class PhysicalInmateCount {
  conductUserId: string;
  countReason: string;
  count: number;
  comment?: string;
}

export class AgencyLocationInmateCount {
  id: number;
	locationId: number;
  status: string;
	initialCount: PhysicalInmateCount;
  recount?: PhysicalInmateCount;
}
