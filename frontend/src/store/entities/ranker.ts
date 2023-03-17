import Decimal from "break_infinity.js";

export type RankerData = {
  accountId: number;
  username: string;
  rank: number;
  points: Decimal;
  power: Decimal;
  bias: number;
  multi: number;
  growing: boolean;
  assholeTag: string;
  assholePoints: number;
  grapes: Decimal;
  vinegar: Decimal;
  autoPromote: boolean;
};

export class Ranker implements RankerData {
  accountId: number = 0;
  username: string = "";
  rank: number = 0;
  points: Decimal = new Decimal(0);
  power: Decimal = new Decimal(1);
  bias: number = 0;
  multi: number = 1;
  growing: boolean = true;
  assholeTag: string = "";
  assholePoints: number = 0;
  grapes: Decimal = new Decimal(0);
  vinegar: Decimal = new Decimal(0);
  autoPromote: boolean = false;

  constructor(data: any) {
    Object.assign(this, data);
    this.points = Object.freeze(new Decimal(this.points));
    this.power = Object.freeze(new Decimal(this.power));
    this.grapes = Object.freeze(new Decimal(this.grapes));
    this.vinegar = Object.freeze(new Decimal(this.vinegar));
  }

  getPowerPerSecond(): Decimal {
    if (this.rank === 1 || !this.growing) return new Decimal(0);
    return new Decimal((this.bias + this.rank - 1) * this.multi);
  }
}
