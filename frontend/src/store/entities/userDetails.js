import Cookies from "js-cookie";

class UserDetails {
  constructor(data) {
    this.uuid = data.uuid;
    this.accountId = data.accountId;
    this.highestCurrentLadder = data.highestCurrentLadder;
    Cookies.set("_uuid", this.uuid, { expires: 10 * 365 });
  }
}

export default UserDetails;
