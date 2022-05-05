class Version {
  /**
   *
   * @param {"MAJOR"|"MINOR"|"PATCH"} type - the type of version update
   * @param {string} name - the short name for this update
   * @param {string[]} [features] - a list of features added in that version
   * @param {string[]} [balancing] - a list of balancing changes added in that version
   * @param {string[]} [improvements] - a list of improvements to existing features added in that version
   * @param {string[]} [fixes] - a list of bugfixes added in that version
   */
  constructor(type, name, { features, balancing, improvements, fixes }) {
    if (type !== "MAJOR" && type !== "MINOR" && type !== "PATCH") {
      throw new Error(`Version type '${type}' is not known.`);
    }
    this.type = type;
    this.name = name ? name : "";
    this.changes = {
      features: features ? features : [],
      balancing: balancing ? balancing : [],
      improvements: improvements ? improvements : [],
      fixes: fixes ? fixes : [],
    };
  }
}

export default Version;
