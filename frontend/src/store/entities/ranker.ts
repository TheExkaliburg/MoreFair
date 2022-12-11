import Decimal from "break_infinity.js";

export type RankerData = {
  id: number;
  username: string;
  rank: number;
  points: Decimal;
  power: Decimal;
  bias: number;
  multi: number;
  growing: boolean;
  autoPromote: boolean;
  assholeTag: string;
  grapes: Decimal;
  vinegar: Decimal;
};

export class Ranker implements RankerData {
  id: number = 0;
  username: string = "";
  rank: number = 0;
  points: Decimal = new Decimal(0);
  power: Decimal = new Decimal(1);
  bias: number = 0;
  multi: number = 1;
  growing: boolean = true;
  autoPromote: boolean = false;
  assholeTag: string = "";
  grapes: Decimal = new Decimal(0);
  vinegar: Decimal = new Decimal(0);

  constructor(data: any) {
    Object.assign(this, data);
    Object.freeze(this.points);
    Object.freeze(this.power);
    Object.freeze(this.grapes);
    Object.freeze(this.vinegar);
  }
}
