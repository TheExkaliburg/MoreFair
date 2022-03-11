import Decimal from "break_infinity.js";

//
class Ranker {
  constructor(data) {
    this.accountId = data.accountId;
    this.username = data.username;
    this.rank = data.rank;
    this.points = new Decimal(data.points);
    this.power = new Decimal(data.power);
    this.bias = data.bias;
    this.multiplier = data.multiplier;
    this.you = data.you;
    this.growing = data.growing;
    this.autoPromote = data.autoPromote;

    if (this.you) {
      this.grapes = new Decimal(data.grapes);
      this.vinegar = new Decimal(data.vinegar);
    } else {
      this.grapes = new Decimal(0);
      this.vinegar = new Decimal(0);
    }
  }
}

export default Ranker;
