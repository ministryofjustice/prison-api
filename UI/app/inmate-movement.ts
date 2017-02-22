export class InmateMovement {

  moveCategory: string
  inmateId: string;
  moveDateTime: string;

  /// external movements (moveCategory='external')
  fromAgencyId?: string;
  toAgencyId?: string;
  moveType?: string;
  moveReason?: string;

  /// internal movements (movecategory='internal')
  fromLocationId?: string;
  toLocationId?: string;
}
