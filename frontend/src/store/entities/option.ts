abstract class Option<T> {
  isActive: () => boolean;
  callback: (value: T, oldValue: T) => void;
  value: T;

  constructor(value: T) {
    this.isActive = () => true;
    this.callback = () => {};
    this.value = value;
  }

  get(): T {
    return this.value;
  }

  set(value: T): void {
    const oldValue = this.value;
    this.value = value;
    if (this.callback) this.callback(value, oldValue);
  }

  setCallback(callback: (value: T, oldValue: T) => void): Option<T> {
    this.callback = callback;
    return this;
  }

  setIsActive(isActive: () => boolean): Option<T> {
    this.isActive = isActive;
    return this;
  }

  static functions(): string[] {
    return ["callback", "isActive", "isVisited"];
  }
}

export class OptionsGroup {
  [key: string]: Option<any>;

  constructor(options: { [key: string]: Option<any> }) {
    Object.assign(this, options);
  }
}

export class BooleanOption extends Option<boolean> {}

export class IntegerOption extends Option<number> {}

export class RangeOption extends Option<number> {
  transient: {
    min: number;
    max: number;
  } = { min: 0, max: 100 };

  constructor(value: number, min: number, max: number) {
    super(value);
    this.transient.min = min;
    this.transient.max = max;
  }
}

export class EnumOption extends Option<string> {
  transient: {
    options: string[];
  } = { options: [] };

  constructor(value: string, options: string[]) {
    super(value);
    this.transient = { options };
  }
}

export class EditableStringListOption extends Option<string[]> {}

export class EditableMentionsOption extends EditableStringListOption {}

export class EditableIgnoreOption extends EditableStringListOption {}

export class EditableThemeURLOption extends EditableStringListOption {}

export const ObjectFunctions = ["callback", "isActive"];
