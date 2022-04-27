import Cookies from "js-cookie";

class UserDetails {
  constructor(data) {
    this.uuid = data.uuid;
    this.accountId = data.accountId;
    this.highestCurrentLadder = data.highestCurrentLadder;
    this.accessRole = data.accessRole;
  }

  static placeholder() {
    return new UserDetails({
      uuid: "",
      accountId: 1,
      highestCurrentLadder: 1,
      accessRole: "PLAYER",
    });
  }

  saveUUID() {
    Cookies.set("_uuid", this.uuid, {
      expires: 10 * 365,
      secure: true,
    });
  }
}

export default UserDetails;
