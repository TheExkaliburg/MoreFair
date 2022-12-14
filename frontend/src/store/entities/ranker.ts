import Decimal from "break_infinity.js";

export type RankerData = {
  id: number;
  username: string;
  you: boolean;
  rank: number;
  points: Decimal;
  power: Decimal;
  bias: number;
  multi: number;
  growing: boolean;
  autoPromote: boolean;
  assholeTag: string;
  assholePoints: number;
  grapes: Decimal;
  vinegar: Decimal;
};

export class Ranker implements RankerData {
  id: number = 0;
  username: string = "";
  you: boolean = false;
  rank: number = 0;
  points: Decimal = new Decimal(0);
  power: Decimal = new Decimal(1);
  bias: number = 0;
  multi: number = 1;
  growing: boolean = true;
  autoPromote: boolean = false;
  assholeTag: string = "";
  assholePoints: number = 0;
  grapes: Decimal = new Decimal(0);
  vinegar: Decimal = new Decimal(0);

  constructor(data: any) {
    Object.assign(this, data);
    this.points = Object.freeze(new Decimal(this.points));
    this.power = Object.freeze(new Decimal(this.power));
    this.grapes = Object.freeze(new Decimal(this.grapes));
    this.vinegar = Object.freeze(new Decimal(this.vinegar));
  }
}
