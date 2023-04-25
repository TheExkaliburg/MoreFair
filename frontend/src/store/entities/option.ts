abstract class Option<T> {
  isVisited: () => boolean;
  isActive: () => boolean;
  value: T;

  constructor(value: T) {
    this.isVisited = () => true;
    this.isActive = () => true;
    this.value = value;
  }

  get(): T {
    return this.value;
  }

  set(value: T): void {
    this.value = value;
  }
}

export class OptionsGroup {
  [key: string]: Option<any>;

  constructor(options: { [key: string]: Option<any> }) {
    Object.assign(this, options);
  }
}

export class BooleanOption extends Option<boolean> {}

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
