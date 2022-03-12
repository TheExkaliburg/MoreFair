class Option {
  //eslint-disable-next-line no-unused-vars
  constructor(payload) {}

  get() {
    throw new Error("Method not implemented.");
  }

  //eslint-disable-next-line no-unused-vars
  set(payload) {
    throw new Error("Method not implemented.");
  }
}

export class BoolOption extends Option {
  constructor({ name, value }) {
    super();
    this.name = name;
    this.value = value;
  }

  get() {
    return this.value;
  }

  set({ value }) {
    if (typeof value !== "boolean") {
      throw new Error(`Value must be a boolean but is "${typeof value}"`);
    }
    this.value = value;
  }
}

export class RangeOption extends Option {
  constructor({ name, value, min, max }) {
    super();
    this.name = name;
    this.value = value;
    this.min = min;
    this.max = max;
  }

  get() {
    return this.value;
  }

  set({ value = this.value, min = this.min, max = this.max }) {
    this.min = min;
    this.max = max;
    this.value = value;
    if (value < min) {
      this.value = min;
    }
    if (value > max) {
      this.value = max;
    }
  }
}
