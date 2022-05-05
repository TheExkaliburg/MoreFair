class Option {
  //eslint-disable-next-line no-unused-vars
  constructor(payload) {
    this.isVisible = () => true;
    this.isActive = () => true;
  }

  get() {
    throw new Error("Method not implemented.");
  }

  //eslint-disable-next-line no-unused-vars
  set(payload) {
    throw new Error("Method not implemented.");
  }

  setVisibleFn(fn) {
    this.isVisible = fn;
    return this;
  }

  setActiveFn(fn) {
    this.isActive = fn;
    return this;
  }
}

export class BoolOption extends Option {
  constructor({ displayName, name, value }) {
    super();
    this.displayName = displayName;
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
  constructor({ displayName, name, value, min, max }) {
    super();
    this.displayName = displayName;
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

export class NumberOption extends Option {
  constructor({ displayName, name, value, min, max }) {
    super();
    this.displayName = displayName;
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
    if (min && value < min) {
      this.value = min;
    }
    if (max && value > max) {
      this.value = max;
    }
  }
}

export class IntegerOption extends NumberOption {
  constructor({ displayName, name, value, min, max }) {
    super({ displayName, name, value, min, max });
  }

  get() {
    return super.get();
  }

  set({ value = this.value, min = this.min, max = this.max }) {
    value = Math.floor(value);
    super.set({ value, min, max });
  }
}

export class DropdownOption extends Option {
  constructor({ displayName, name, options, callback }) {
    super({ displayName, name, options, callback });
    this.displayName = displayName;
    this.name = name;
    this.options = options;
    this.selectedIndex = 0;
    if (callback) this.callback = callback;
    else this.callback = () => {};
  }

  get value() {
    return this.get();
  }

  set value(val) {
    this.set({ selectedIndex: Math.max(this.options.indexOf(val), 0) });
  }

  get() {
    return this.options[this.selectedIndex];
  }

  set({
    selectedIndex = this.selectedIndex,
    options = this.options,
    callback = this.callback,
  }) {
    this.selectedIndex = selectedIndex;
    if (options) {
      this.options = options;
    }
    if (this.callback) {
      this.callback(this);
    }
    if (callback) {
      this.callback = callback;
    }
  }
}
export class OptionSection {
  constructor({ displayName, name, options }) {
    this.displayName = displayName;
    this.name = name;
    this.options = options;
    this.isVisible = () => true;
  }

  setVisibleFn(fn) {
    this.isVisible = fn;
    return this;
  }
}
