import Ranker from "@/ladder/entities/ranker";

export default class Ladder {
  constructor(data) {
    this.rankers = data.rankers;
    this.rankers.forEach((r) => {
      r = new Ranker(r);
    });
  }
}
