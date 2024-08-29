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
  wine: Decimal;
  autoPromote: boolean;
};

export class Ranker implements RankerData {
  accountId = 0;
  username = "";
  rank = 0;
  points: Decimal = new Decimal(0);
  power: Decimal = new Decimal(1);
  bias = 0;
  multi = 1;
  growing = true;
  assholeTag = "";
  assholePoints = 0;
  grapes: Decimal = new Decimal(0);
  vinegar: Decimal = new Decimal(0);
  wine: Decimal = new Decimal(0);
  autoPromote = false;

  constructor(data: any) {
    Object.assign(this, data);
    this.points = Object.freeze(new Decimal(this.points));
    this.power = Object.freeze(new Decimal(this.power));
    this.grapes = Object.freeze(new Decimal(this.grapes));
    this.vinegar = Object.freeze(new Decimal(this.vinegar));
    this.wine = Object.freeze(new Decimal(this.wine));
  }

  getPowerPerSecond(rank: number = this.rank): Decimal {
    if (rank === 1 || !this.growing) return new Decimal(0);
    return new Decimal((this.bias + rank - 1) * this.multi);
  }
}
