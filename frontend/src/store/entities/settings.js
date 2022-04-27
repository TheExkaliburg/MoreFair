import Decimal from "break_infinity.js";

class Settings {
  constructor(data) {
    this.pointsForPromote = new Decimal(data.pointsForPromote);
    this.minimumPeopleForPromote = data.minimumPeopleForPromote;
    this.assholeLadder = data.assholeLadder;
    this.assholeTags = data.assholeTags;
    this.baseVinegarNeededToThrow = new Decimal(data.baseVinegarNeededToThrow);
    this.baseGrapesNeededToAutoPromote = new Decimal(
      data.baseGrapesNeededToAutoPromote
    );
    this.manualPromoteWaitTime = data.manualPromoteWaitTime;
    this.autoPromoteLadder = data.autoPromoteLadder;
  }

  static placeholder() {
    return new Settings({
      pointsForPromote: new Decimal(Infinity),
      minimumPeopleForPromote: Infinity,
      assholeLadder: 1,
      assholeTags: [""],
      baseVinegarNeededToThrow: new Decimal(Infinity),
      baseGrapesNeededToAutoPromote: new Decimal(Infinity),
      manualPromoteWaitTime: Infinity,
      autoPromoteLadder: Infinity,
    });
  }
}

export default Settings;
