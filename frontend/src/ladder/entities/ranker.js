import Decimal from "break_infinity.js";

//
class Ranker {
  constructor(data) {
    if (!data) data = Ranker.placeholder();
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
    this.timesAsshole = data.timesAsshole;

    if (this.you) {
      this.grapes = new Decimal(data.grapes);
      this.vinegar = new Decimal(data.vinegar);
    } else {
      this.grapes = new Decimal(0);
      this.vinegar = new Decimal(0);
    }
  }

  static placeholder() {
    return new Ranker({
      accountId: 0,
      username: "",
      rank: 1,
      points: new Decimal(0),
      power: new Decimal(1),
      bias: 0,
      multiplier: 1,
      you: false,
      growing: true,
      autoPromote: false,
      grapes: new Decimal(0),
      vinegar: new Decimal(0),
    });
  }
}

export default Ranker;
